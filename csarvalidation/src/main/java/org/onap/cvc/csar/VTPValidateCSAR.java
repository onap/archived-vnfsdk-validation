/*
 * Copyright 2019 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.cvc.csar;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.output.OnapCommandResultType;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.oclip.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates CSAR
 */
@OnapCommandSchema(schema = "vtp-validate-csar.yaml")
public class VTPValidateCSAR extends OnapCommand {

    private static final Gson GSON = new Gson();
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSAR.class);
    private static final String RELEASE_ATTRIBUTE_NAME = "release";
    private static final String PNF_ATTRIBUTE_NAME = "pnf";
    private static final String CSAR_ATTRIBUTE_NAME = "csar";
    private static final String RULES_ATTRIBUTE_NAME = "rules";

    public static class CSARValidation {

        public static class VNF {

            private String name;
            private String vendor;
            private String version;
            private String type;
            private String mode;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getVendor() {
                return vendor;
            }

            public void setVendor(String vendor) {
                this.vendor = vendor;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getMode() {
                return mode;
            }

            public void setMode(String mode) {
                this.mode = mode;
            }
        }

        private VNF vnf = new VNF();
        private String date;
        private String criteria;

        public static class Result {

            private boolean passed;
            private String vnfreqName;
            private String description;
            private final List<CSARError> errors = new ArrayList<>();
            private final List<CSARError> warnings = new ArrayList<>();

            public boolean isPassed() {
                return passed;
            }

            public void setPassed(boolean passed) {
                this.passed = passed;
            }

            public String getVnfreqName() {
                return vnfreqName;
            }

            public void setVnfreqName(String vnfreqName) {
                this.vnfreqName = vnfreqName;
            }

            public List<CSARError> getErrors() {
                return Collections.unmodifiableList(errors);
            }

            public void addError(CSARError error) {
                this.errors.add(error);
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public List<CSARError> getWarnings() {
                return Collections.unmodifiableList(warnings);
            }

            public void addErrorAsWarning(CSARError error) {
                this.warnings.add(error);
            }

            public void addErrors(List<CSARError> errors) {
                this.errors.addAll(errors);
            }

            public void addErrorsAsWarnings(List<CSARError> warnings) {
                this.warnings.addAll(warnings);
            }
        }

        private List<Result> results = new ArrayList<>();

        private String contact = "ONAP VTP Team onap-discuss@lists.onap.org";

        private String platform = "VNFSDK - VNF Test Platform (VTP) 1.0";

        public VNF getVnf() {
            return vnf;
        }

        public void setVnf(VNF vnf) {
            this.vnf = vnf;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCriteria() {
            return criteria;
        }

        public void setCriteria(String criteria) {
            this.criteria = criteria;
        }

        public List<Result> getResults() {
            return results;
        }

        public void setResults(List<Result> results) {
            this.results = results;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }
    }

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(VTPValidateCSAR.class.getResourceAsStream("/vnfreqs.properties"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String release = (String) getParametersMap().get(RELEASE_ATTRIBUTE_NAME).getValue();
        String pathToCsarFile = (String) getParametersMap().get(CSAR_ATTRIBUTE_NAME).getValue();
        boolean isPnf = (boolean) getParametersMap().get(PNF_ATTRIBUTE_NAME).getValue();
        String rulesToValidate = (String) getParametersMap().get(RULES_ATTRIBUTE_NAME).getValue();

        boolean overallPass = true;
        try (CSARArchive csar = isPnf ? new PnfCSARArchive() : new CSARArchive()) {
            csar.init(pathToCsarFile);
            csar.parse();

            CSARValidation validation = createCsarValidationFor(csar);

            String keyErrors = isPnf ? "pnferrors.ignored" : "vnferrors.ignored";
            List<String> ignoreCodes = this.getPropertiesList(keyErrors);

            //Add SOL004 error codes
            CSARValidation.Result resultSOL004 = new CSARValidation.Result();
            resultSOL004.setVnfreqName("SOL004");
            resultSOL004.setDescription(csar.getSOL004Version());

            for (CSARError error : csar.getErrors()) {
                if (!ignoreCodes.contains(error.getCode())) {
                    resultSOL004.addError(error);
                    overallPass = false;
                }
            }
            resultSOL004.setPassed(resultSOL004.getErrors().isEmpty());

            validation.getResults().add(resultSOL004);

            final Pair<Boolean, List<CSARValidation.Result>> validationResult =
                    validate(pathToCsarFile, release, isPnf, ignoreCodes, new RulesToValidate(rulesToValidate));

            overallPass &= validationResult.getLeft();
            validation.getResults().addAll(validationResult.getRight());

            validation.setDate(new Date().toString());
            validation.setCriteria(overallPass ? "PASS" : "FAILED");

            setOperationResult(validation);
        } catch (Exception e) {
            throw new OnapCommandExecutionFailed(e.getMessage(), e);
        }
    }

    private Pair<Boolean,List<CSARValidation.Result>> validate(String path, String release, boolean isPnf, List<String> ignoreCodes, RulesToValidate rulesToValidate) {
        final String keyReqs = isPnf ? "pnfreqs.enabled" : "vnfreqs.enabled";
        final List<String> activeRules = this.getPropertiesList(keyReqs);
        final String releasesOrder = PROPERTIES.getProperty("releases.order");
        final ReleasesResolver releasesResolver = ReleasesResolver.create(releasesOrder);
        final String product = this.getInfo().getProduct();

        return new CsarValidator(new CommandFactory(), ignoreCodes, activeRules, releasesResolver, rulesToValidate)
                .validate(new CsarValidator.ValidationContext(path, product, release, isPnf));
    }

    static CSARValidation createCsarValidationFor(CSARArchive csar) {
        //Fill up the basic details
        CSARValidation validation = new CSARValidation();
        validation.getVnf().setName(csar.getProductName());
        validation.getVnf().setVendor(csar.getVendorName());
        validation.getVnf().setVersion(csar.getVersion());
        validation.getVnf().setType("TOSCA");
        validation.getVnf().setMode(csar.getToscaMeta().getMode().name());
        return validation;
    }

    private void setOperationResult(CSARValidation validation) {
        this.getResult().getRecordsMap().get("vnf").getValues().add(
            GSON.toJson(validation.getVnf()));
        this.getResult().getRecordsMap().get("date").getValues().add(validation.getDate());
        this.getResult().getRecordsMap().get("criteria").getValues().add(validation.getCriteria());
        this.getResult().getRecordsMap().get("results").getValues().add(
            GSON.toJson(validation.getResults()));

        this.getResult().setOutput(GSON.toJson(validation));
        this.getResult().setType(OnapCommandResultType.TEXT);
    }

    private List<String> getPropertiesList(String key) {
        String[] enabledReqs = PROPERTIES.getProperty(key, "").split(",");
        return Arrays.stream(enabledReqs).filter(it->!it.isEmpty()).collect(Collectors.toList());
    }
}
