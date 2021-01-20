/*
 * Copyright 2021 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cvc.csar.oclip.Command;
import org.onap.cvc.csar.oclip.CommandFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsarValidator {

    private final CommandFactory commandFactory;
    private final List<String> ignoreCodes;
    private final List<String> activeRules;
    private final ReleasesResolver releasesResolver;
    private final RulesToValidate rulesToValidate;

    public CsarValidator(CommandFactory commandFactory, List<String> ignoreCodes,
                         List<String> activeRules, ReleasesResolver releasesResolver,
                         RulesToValidate rulesToValidate) {
        this.commandFactory = commandFactory;
        this.ignoreCodes = ignoreCodes;
        this.activeRules = activeRules;
        this.releasesResolver = releasesResolver;
        this.rulesToValidate = rulesToValidate;
    }

    Pair<Boolean, List<VTPValidateCSAR.CSARValidation.Result>> validate(ValidationContext validationContext) {

        boolean overallPass = true;
        List<VTPValidateCSAR.CSARValidation.Result> results = new ArrayList<>();

        final List<String> rules = getRulesToExecute();

        for (String rule : rules) {
                final Pair<Boolean, List<VTPValidateCSAR.CSARValidation.Result>> pair = executeValidation(releasesResolver, ignoreCodes, rule, validationContext);
                overallPass &= pair.getLeft();
                results.addAll(pair.getRight());
        }

        return Pair.of(overallPass, results);
    }

    private List<String> getRulesToExecute() {
        final List<String> rulesToExecute = rulesToValidate.get();

        return activeRules.stream().filter(
                it -> rulesToExecute.isEmpty() || rulesToExecute.contains(it)
        ).collect(Collectors.toList());
    }

    private Pair<Boolean, List<VTPValidateCSAR.CSARValidation.Result>> executeValidation(
            ReleasesResolver releasesResolver, List<String> ignoreCodes,
            String reqName, ValidationContext validationContext) {

        boolean overallPass = true;
        List<VTPValidateCSAR.CSARValidation.Result> results = new ArrayList<>();
        VTPValidateCSAR.CSARValidation.Result result = new VTPValidateCSAR.CSARValidation.Result();
        result.setVnfreqName(reqName);

        try {
            Command cmd = getCommand(reqName, validationContext);
            if (releasesResolver.resolveWithAncestors(validationContext.getRelease()).contains(cmd.getRelease())) {
                final List<CSARArchive.CSARError> errors = cmd.run();

                result.setDescription(cmd.getDescription());

                final Pair<List<CSARArchive.CSARError>, List<CSARArchive.CSARError>> segregateErrors = segregate(errors, ignoreCodes, reqName);
                overallPass = segregateErrors.getLeft().isEmpty();
                result.addErrors(segregateErrors.getLeft());
                result.addErrorsAsWarnings(segregateErrors.getRight());
                result.setPassed(result.getErrors().isEmpty());
                results.add(result);
            }
        } catch (Exception e) {
            result.setPassed(false);
            overallPass = false;
            result.addError(new CSARArchive.CSARErrorUnknown(e.getMessage()));
            results.add(result);
        }
        return Pair.of(overallPass, results);
    }

    private Command getCommand(String reqName, ValidationContext validationContext) throws OnapCommandException {
        return validationContext.isPnf() ? commandFactory.createForPnf(reqName, validationContext.getPathToCsar(), validationContext.getProduct()) : commandFactory.createForVnf(reqName, validationContext.getPathToCsar(), validationContext.getProduct());
    }

    private Pair<List<CSARArchive.CSARError>, List<CSARArchive.CSARError>> segregate(
            List<CSARArchive.CSARError> errors,  List<String> ignoreCodes, String reqName){

        final List<CSARArchive.CSARError> errorsList = new ArrayList<>();
        final List<CSARArchive.CSARError> warningsList = new ArrayList<>();

        for (CSARArchive.CSARError error : errors) {
            if (!isErrorIgnored(ignoreCodes, reqName, error)) {
                errorsList.add(error);
            } else {
                warningsList.add(error);
            }
        }

        return Pair.of(errorsList,warningsList);
    }

    private boolean isErrorIgnored(List<String> ignoreCodes, String reqName, CSARArchive.CSARError error) {
        return ignoreCodes.contains(error.getCode()) || ignoreCodes.contains(reqName + "-" + error.getCode());
    }

    public static class ValidationContext {
        private final String pathToCsar;
        private final String product;
        private final String release;
        private final boolean isPnf;


        public ValidationContext(String pathToCsar, String product, String release, boolean isPnf) {
            this.pathToCsar = pathToCsar;
            this.product = product;
            this.release = release;
            this.isPnf = isPnf;
        }

        public String getPathToCsar() {
            return pathToCsar;
        }

        public String getProduct() {
            return product;
        }

        public String getRelease() {
            return release;
        }

        public boolean isPnf() {
            return isPnf;
        }
    }
}
