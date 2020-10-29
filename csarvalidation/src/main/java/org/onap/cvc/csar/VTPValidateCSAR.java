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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.error.OnapCommandInvalidParameterValue;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.output.OnapCommandResultType;
import org.onap.cli.fw.registrar.OnapCommandRegistrar;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates CSAR
 */
@OnapCommandSchema(schema = "vtp-validate-csar.yaml")
public class VTPValidateCSAR extends OnapCommand {

    private static Gson gson = new Gson();
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSAR.class);
    private static final String RULES_ATTRIBUTE_SEPARATOR = ",";
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
            private List<CSARError> errors = new ArrayList<>();
            private List<CSARError> warnings = new ArrayList<>();

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

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(VTPValidateCSAR.class.getResourceAsStream("/vnfreqs.properties"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get(CSAR_ATTRIBUTE_NAME).getValue();
        boolean isPnf = (boolean) getParametersMap().get(PNF_ATTRIBUTE_NAME).getValue();
        String rulesToValidate = (String) getParametersMap().get(RULES_ATTRIBUTE_NAME).getValue();

        boolean overallPass = true;
        try (CSARArchive csar = isPnf ? new PnfCSARArchive() : new CSARArchive()) {
            csar.init(path);
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

            String keyReqs = isPnf ? "pnfreqs.enabled" : "vnfreqs.enabled";
            List<String> activeRules = this.getPropertiesList(keyReqs);
            if(rulesToValidate.isEmpty()) {
                // Run thru the vnfreqs requirement checks
                for (String vnfreq : activeRules) {
                    CSARValidation.Result result = new CSARValidation.Result();
                    result.setVnfreqName(vnfreq);
                    overallPass = validateVnfOrPnf(path, validation, ignoreCodes, vnfreq, result, isPnf, overallPass);
                }
            } else {
                // Validate selected rules
                String[] listOfRulesToValidate = rulesToValidate.split(RULES_ATTRIBUTE_SEPARATOR);
                for (String rule : listOfRulesToValidate) {
                    if(activeRules.contains(rule)) {
                        CSARValidation.Result result = new CSARValidation.Result();
                        result.setVnfreqName(rule);
                        overallPass = validateVnfOrPnf(path, validation, ignoreCodes, rule, result, isPnf, overallPass);
                    }
                }
            }

            validation.setDate(new Date().toString());
            validation.setCriteria(overallPass ? "PASS" : "FAILED");

            setOperationResult(validation);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }
    }

    private boolean validateVnfOrPnf(String path, CSARValidation validation,
        List<String> ignoreCodes, String vnfreq, CSARValidation.Result result, boolean isPnf, boolean overallPass) {
        try {
            String command = "csar-validate-" + vnfreq;
            OnapCommand cmd = OnapCommandRegistrar.getRegistrar().get(command, this.getInfo().getProduct());
            cmd.getParametersMap().get("csar").setValue(path);
            setPnfValueIfAvailable(isPnf, cmd);

            result.setDescription(cmd.getDescription());
            cmd.execute();

            for (CSARError error : (List<CSARError>) cmd.getResult().getOutput()) {
                if (!isErrorIgnored(ignoreCodes, vnfreq, error)) {
                    result.addError(error);
                    overallPass = false;
                } else {
                    result.addErrorAsWarning(error);
                }
            }

            result.setPassed(result.getErrors().isEmpty());
            validation.getResults().add(result);
        } catch (Exception e) {
            result.setPassed(false);
            overallPass = false;
            result.addError(new CSARArchive.CSARErrorUnknown(e.getMessage()));
            validation.getResults().add(result);
        }
        return overallPass;
    }

    private boolean isErrorIgnored(List<String> ignoreCodes, String vnfreq, CSARError error) {
        return ignoreCodes.contains(error.getCode()) || ignoreCodes.contains(vnfreq + "-" + error.getCode());
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

    private void setOperationResult(CSARValidation validation) throws Exception { //NOSONAR
        this.getResult().getRecordsMap().get("vnf").getValues().add(
            gson.toJson(validation.getVnf()));
        this.getResult().getRecordsMap().get("date").getValues().add(validation.getDate());
        this.getResult().getRecordsMap().get("criteria").getValues().add(validation.getCriteria());
        this.getResult().getRecordsMap().get("results").getValues().add(
            gson.toJson(validation.getResults()));

        this.getResult().setOutput(gson.toJson(validation));
        this.getResult().setType(OnapCommandResultType.TEXT);
    }

    private void setPnfValueIfAvailable(boolean isPnf, OnapCommand cmd) throws OnapCommandInvalidParameterValue {
        final OnapCommandParameter pnf = cmd.getParametersMap().get(PNF_ATTRIBUTE_NAME);
        if (pnf != null) {
            pnf.setValue(isPnf);
        }
    }

    private List<String> getPropertiesList(String key) {
        String[] enabledReqs = properties.getProperty(key, "").split(",");
        List<String> list = new ArrayList<>();
        for (String req : enabledReqs) {
            if (!req.isEmpty()) {
                list.add(req);
            }
        }
        return list;
    }
}
