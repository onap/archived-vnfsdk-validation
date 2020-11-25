/*
 * Copyright 2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.validation.yaml;

import org.junit.jupiter.api.Test;
import org.onap.validation.yaml.error.SchemaValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.schema.YamlSchemaFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.validation.yaml.YamlLoadingUtils.VALID_YAML_DOCUMENT_INDEX;
import static org.onap.validation.yaml.YamlLoadingUtils.YAML_DOCUMENT_WITH_MISSING_FIELD_AND_WRONG_VALUE_INDEX;
import static org.onap.validation.yaml.YamlLoadingUtils.YAML_DOCUMENT_WITH_MISSING_FIELD_INDEX;
import static org.onap.validation.yaml.YamlLoadingUtils.YAML_DOCUMENT_WITH_WRONG_VALUE_IN_ARRAY_INDEX;

class YamlValidatorTest {

    @Test
    void shouldCreateValidatorUsingSchemaLoadedFromYamlFileAndValidatedJsonStyleDocumentsFromThatFile()
            throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidJsonStyleMultiDocumentYamlFile();
        YamlValidator validator = new YamlValidator(new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0)));
        Map<Integer, List<SchemaValidationError>> validationErrors = new HashMap<>();

        // when
        for (int documentIndex = 1; documentIndex < documents.size(); documentIndex++) {
            validationErrors.put(documentIndex, validator.validate(documents.get(documentIndex)));
        }

        // then
        assertValidatorReturnedCorrectErrors(validationErrors);
    }

    @Test
    void shouldCreateValidatorUsingSchemaLoadedFromYamlFileAndValidatedDocumentsFromThatFile()
            throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();
        YamlValidator validator = new YamlValidator(new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0)));
        Map<Integer, List<SchemaValidationError>> validationErrors = new HashMap<>();

        // when
        for (int documentIndex = 1; documentIndex < documents.size(); documentIndex++) {
            validationErrors.put(documentIndex, validator.validate(documents.get(documentIndex)));
        }

        // then
        assertValidatorReturnedCorrectErrors(validationErrors);
    }

    private void assertValidatorReturnedCorrectErrors(Map<Integer, List<SchemaValidationError>> validationErrors) {

        SchemaValidationError expectedValidationValueError =
                new SchemaValidationError(
                        "/pmMetaData/pmFields/measResultType",
                        "Value(s) is/are not in array of accepted values.\n"
                                + " value(s):  integer\n"
                                + "  accepted value(s):  [float, uint32, uint64]"
                );
        SchemaValidationError expectedValidationKeyError =
                new SchemaValidationError(
                        "/pmMetaData/pmFields/",
                        "Key not found: measChangeType"
                );
        SchemaValidationError expectedValidationValuesInArrayError =
                new SchemaValidationError(
                        "/pmMetaData/pmFields/measAdditionalFields/vendorField1",
                        "Value(s) is/are not in array of accepted values.\n"
                                + " value(s):  [Z, A]\n"
                                + "  accepted value(s):  [X, Y, Z]"
                );

        assertThat(validationErrors)
                .hasSize(4)
                .containsKeys(1, 2, 3);
        assertThat(validationErrors.get(YAML_DOCUMENT_WITH_MISSING_FIELD_AND_WRONG_VALUE_INDEX))
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .containsAll(List.of(
                        expectedValidationValueError,
                        expectedValidationKeyError
                ));
        assertThat(validationErrors.get(YAML_DOCUMENT_WITH_MISSING_FIELD_INDEX))
                .hasSize(1)
                .usingFieldByFieldElementComparator()
                .contains(expectedValidationKeyError);
        assertThat(validationErrors.get(YAML_DOCUMENT_WITH_WRONG_VALUE_IN_ARRAY_INDEX))
                .hasSize(1)
                .usingFieldByFieldElementComparator()
                .contains(expectedValidationValuesInArrayError);
        assertThat(validationErrors.get(VALID_YAML_DOCUMENT_INDEX)).isEmpty();
    }

}
