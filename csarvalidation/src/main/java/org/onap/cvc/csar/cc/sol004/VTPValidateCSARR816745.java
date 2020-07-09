/*
 * Copyright 2020 Nokia.
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

package org.onap.cvc.csar.cc.sol004;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.validation.yaml.YamlFileValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@OnapCommandSchema(schema = "vtp-validate-csar-r816745.yaml")
public class VTPValidateCSARR816745 extends VTPValidateCSARBase {

    private static final Logger LOGGER =  LoggerFactory.getLogger(VTPValidateCSARR816745.class);

    private static class CSARPmDictionaryValidationError extends CSARArchive.CSARError {

        CSARPmDictionaryValidationError(int documentNumber, String file, String path, String message) {
            super("0x1000");
            this.message = String.format(
                "Invalid YAML document in PM_Dictionary file. %n" +
                    "In document number %s (excluding document with schema) error occur. %n" +
                    "Path: %s%n" +
                    "%s",
                documentNumber, path, message
            );
            this.file = file;
        }

    }

    private static class CSARPmDictionaryLoadingError extends CSARArchive.CSARError {

        CSARPmDictionaryLoadingError(String file, String message) {
            super("0x2000");
            this.message = String.format(
                "Fail to load PM_Dictionary With error: %s",
                message
            );
            this.file = file;
        }

    }

    private static final String PM_DICTIONARY = "onap_pm_dictionary";

    @Override
    protected void validateCSAR(CSARArchive csar) {
        Map<String, Map<String, List<String>>> nonManoFields = csar.getManifest().getNonMano();
        String rootPath = csar.getWorkspace().getPathToCsarFolder().map(Path::toString).orElse("/");
        if (nonManoFields.containsKey(PM_DICTIONARY)) {
            validateYamlFile(rootPath+"/",getLocationOfPmDictionaryFile(nonManoFields));
        }
    }

    private String getLocationOfPmDictionaryFile(Map<String, Map<String, List<String>>> nonManoFields) {
        return nonManoFields.get(PM_DICTIONARY).get("source").get(0);
    }

    private void validateYamlFile(String rootPath, String artifactPath) {
        try {
            List<YamlDocumentValidationError> validationErrors =
                new YamlFileValidator().validateYamlFileWithSchema(rootPath+artifactPath);
            addAllErrorsReportedByVaidator(artifactPath, validationErrors);
        } catch (YamlProcessingException e) {
            LOGGER.error("Failed to load PM_Dictionary file.", e);
            errors.add(new CSARPmDictionaryLoadingError(
                artifactPath,
                e.getMessage()
            ));
        }

    }

    private void addAllErrorsReportedByVaidator(String artifactPath, List<YamlDocumentValidationError> validationErrors) {
        for(YamlDocumentValidationError validationError: validationErrors) {
            addPmDictionaryValidationError(artifactPath, validationError);
        }
    }

    private void addPmDictionaryValidationError(String artifactPath, YamlDocumentValidationError validationError) {
        errors.add(new CSARPmDictionaryValidationError(
            validationError.getYamlDocumentNumber(),
            artifactPath,
            validationError.getPath(),
            validationError.getMessage()
        ));
    }

    @Override
    protected String getVnfReqsNo() {
        return "R816745";
    }

}
