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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.YamlLoadingUtils.PATH_TO_MULTI_DOCUMENT_INVALID_YAML;
import static org.onap.validation.yaml.YamlLoadingUtils.PATH_TO_VALID_JSON_STYLE_YAML;
import static org.onap.validation.yaml.YamlLoadingUtils.PATH_TO_VALID_YAML;
import static org.onap.validation.yaml.YamlLoadingUtils.readFile;

class YamlContentValidatorTest {
    @Nested
    class FromStringPathValidator {
        @Test
        void shouldReturnCorrectErrorsWhenGivenPathToValidPmDictionaryFile() throws YamlProcessingException {
            // given
            String path = getFullPathForGivenResources(PATH_TO_VALID_YAML);

            // when
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(path);

            // then
            assertValidationReturnedExpectedErrors(validationErrors);
        }

        @Test
        void shouldReturnCorrectErrorsWhenGivenPathToValidJsonStylePmDictionaryFile() throws YamlProcessingException {
            // given
            String path = getFullPathForGivenResources(PATH_TO_VALID_JSON_STYLE_YAML);

            // when
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(path);

            // then
            assertValidationReturnedExpectedErrors(validationErrors);
        }

        @Test
        void shouldThrowErrorWhenGivenPathToInvalidPmDictionaryFile() {
            // given
            String path = getFullPathForGivenResources(PATH_TO_MULTI_DOCUMENT_INVALID_YAML);

            // when /then
            assertThatThrownBy(() -> new YamlContentValidator().validate(path))
                    .isInstanceOf(ParserException.class)
                    .hasMessageContaining("expected the node content, but found '<document end>'");
        }

        @Test
        void shouldThrowErrorWhenGivenInvalidPath() {
            // given
            String path = "invalid/path/to/pm_dictionary";

            // when /then
            assertThatThrownBy(() -> new YamlContentValidator().validate(path))
                    .isInstanceOf(YamlProcessingException.class)
                    .hasMessageContaining("PM_Dictionary YAML file is empty");
        }
    }

    @Nested
    class FromByteArrayValidator {
        @Test
        void shouldReturnCorrectErrorsWhenGivenPmDictionaryFileWithErrors() throws YamlProcessingException, IOException {
            // given
            byte[] yaml = readFile(PATH_TO_VALID_YAML);

            // when
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(yaml);

            // then
            assertValidationReturnedExpectedErrors(validationErrors);
        }

        @Test
        void shouldReturnCorrectErrorsWhenGivenValidJsonStylePmDictionary() throws YamlProcessingException, IOException {
            // given
            byte[] yaml = readFile(PATH_TO_VALID_JSON_STYLE_YAML);

            // when
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(yaml);

            // then
            assertValidationReturnedExpectedErrors(validationErrors);
        }

        @Test
        void shouldThrowErrorWhenGivenInvalidPmDictionary() throws IOException {
            // given
            byte[] yaml = readFile(PATH_TO_MULTI_DOCUMENT_INVALID_YAML);

            // when /then
            assertThatThrownBy(() -> new YamlContentValidator().validate(yaml))
                    .isInstanceOf(ParserException.class)
                    .hasMessageContaining("expected the node content, but found '<document end>'");
        }

        @Test
        void shouldThrowErrorWhenGivenEmptyPmDictionary() {
            // when /then
            assertThatThrownBy(() -> new YamlContentValidator().validate(new byte[0]))
                    .isInstanceOf(YamlProcessingException.class)
                    .hasMessageContaining("PM_Dictionary YAML file is empty");
        }
    }

    private void assertValidationReturnedExpectedErrors(List<YamlDocumentValidationError> validationErrors) {
        assertThat(validationErrors)
                .isNotNull()
                .hasSize(4)
                .usingRecursiveFieldByFieldElementComparator()
                .containsAll(
                        List.of(
                                new YamlDocumentValidationError(1,
                                        "/pmMetaData/pmFields/measResultType",
                                        "Value(s) is/are not in array of accepted values.\n" +
                                                " value(s):  integer\n" +
                                                "  accepted value(s):  [float, uint32, uint64]"),
                                new YamlDocumentValidationError(1,
                                        "/pmMetaData/pmFields/",
                                        "Key not found: measChangeType"),
                                new YamlDocumentValidationError(2,
                                        "/pmMetaData/pmFields/",
                                        "Key not found: measChangeType"),
                                new YamlDocumentValidationError(3,
                                        "/pmMetaData/pmFields/measAdditionalFields/vendorField1",
                                        "Value(s) is/are not in array of accepted values.\n" +
                                                " value(s):  [Z, A]\n" +
                                                "  accepted value(s):  [X, Y, Z]")
                        )
                );
    }

    private String getFullPathForGivenResources(String pathToValidYaml) {
        return this.getClass().getClassLoader().getResource(pathToValidYaml).getPath();
    }
}