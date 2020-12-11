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
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlLoaderTest {
    private static final int EXPECTED_NUMBER_OF_DOCUMENTS = 5;
    private static final String LETTER_S_WITH_ASCII_CODE = "s(115)";

    @Nested
    class FromUrlLoader {
        @Test
        void shouldLoadAllDocumentsFromYamlFile() throws YamlDocumentFactory.YamlDocumentParsingException {
            // when
            List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();

            // then
            assertThat(documents).hasSize(EXPECTED_NUMBER_OF_DOCUMENTS);
        }

        @Test
        void shouldLoadAllDocumentsFromJsonStyleYamlFile() throws YamlDocumentFactory.YamlDocumentParsingException {
            // when
            List<YamlDocument> documents = YamlLoadingUtils.loadValidJsonStyleMultiDocumentYamlFile();

            // then
            assertThat(documents).hasSize(EXPECTED_NUMBER_OF_DOCUMENTS);
        }

        @Test
        void shouldThrowExceptionWhenLoadingDocumentsFromInvalidYamlFile() {
            // when then
            assertThatThrownBy(YamlLoadingUtils::tryToLoadMultiDocumentInvalidYamlFile)
                    .isInstanceOf(ParserException.class)
                    .hasMessageContaining("expected the node content, but found '<document end>'");
        }

        @Test
        void shouldThrowExceptionWhenLoadingInvalidYamlFileWithIncorrectKeyMapping() {
            // when then
            assertThatThrownBy(YamlLoadingUtils::tryToLoadInvalidYamlFileWithIncorrectKeyMapping)
                    .isInstanceOf(ScannerException.class)
                    .hasMessageContaining("mapping values are not allowed here");
        }

        @Test
        void shouldThrowExceptionWhenLoadingInvalidYamlFileWithUnknownEscapeCharacter() {
            // when then
            assertThatThrownBy(YamlLoadingUtils::tryToLoadInvalidYamlFileWithUnknownEscapeCharacter)
                    .isInstanceOf(ScannerException.class)
                    .hasMessageContaining("found unknown escape character " + LETTER_S_WITH_ASCII_CODE);
        }
    }

    @Nested
    class FromStringPathLoader {
        @Test
        void shouldLoadAllDocumentsFromYamlFileUsingPathInString() throws YamlProcessingException {
            // when
            List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFileUsingStringPath();

            // then
            assertThat(documents).hasSize(EXPECTED_NUMBER_OF_DOCUMENTS);
        }

        @Test
        void shouldThrowExceptionWhenLoadingDocumentsFromInvalidYamlFileUsingPathInString() {
            // when then
            assertThatThrownBy(YamlLoadingUtils::tryToLoadMultiDocumentInvalidYamlFileUsingStringPath)
                    .isInstanceOf(ParserException.class)
                    .hasMessageContaining("expected the node content, but found '<document end>'");
        }
    }

    @Nested
    class FromByteArrayLoader {
        private final YamlLoader YAML_LOADER = new YamlLoader(new YamlDocumentFactory());

        @Test
        void shouldLoadAllDocumentsFromYamlFile() throws YamlDocumentFactory.YamlDocumentParsingException, IOException {
            // when
            List<YamlDocument> documents = YAML_LOADER.loadMultiDocumentYaml(YamlLoadingUtils.readFile(YamlLoadingUtils.PATH_TO_YAML_WITH_WRONG_VALUES));

            // then
            assertThat(documents).hasSize(EXPECTED_NUMBER_OF_DOCUMENTS);
        }

        @Test
        void shouldLoadAllDocumentsFromJsonStyleYamlFile() throws YamlDocumentFactory.YamlDocumentParsingException, IOException {
            // when
            List<YamlDocument> documents = YAML_LOADER.loadMultiDocumentYaml(YamlLoadingUtils.readFile(YamlLoadingUtils.PATH_TO_VALID_JSON_STYLE_YAML));

            // then
            assertThat(documents).hasSize(EXPECTED_NUMBER_OF_DOCUMENTS);
        }

        @Test
        void shouldThrowExceptionWhenLoadingDocumentsFromInvalidYamlFile() {
            // when then
            assertThatThrownBy(() -> YAML_LOADER.loadMultiDocumentYaml(YamlLoadingUtils.readFile(YamlLoadingUtils.PATH_TO_MULTI_DOCUMENT_INVALID_YAML)))
                    .isInstanceOf(ParserException.class)
                    .hasMessageContaining("expected the node content, but found '<document end>'");
        }

        @Test
        void shouldThrowExceptionWhenLoadingInvalidYamlFileWithIncorrectKeyMapping() {
            // when then
            assertThatThrownBy(() -> YAML_LOADER.loadMultiDocumentYaml(YamlLoadingUtils.readFile(YamlLoadingUtils.PATH_TO_INVALID_YAML_WITH_INCORRECT_KEY_MAPPING)))
                    .isInstanceOf(ScannerException.class)
                    .hasMessageContaining("mapping values are not allowed here");
        }

        @Test
        void shouldThrowExceptionWhenLoadingInvalidYamlFileWithUnknownEscapeCharacter() {
            // when then
            assertThatThrownBy(() -> YAML_LOADER.loadMultiDocumentYaml(YamlLoadingUtils.readFile(YamlLoadingUtils.PATH_TO_INVALID_YAML_WITH_UNKNOWN_ESCAPE_CHARACTER)))
                    .isInstanceOf(ScannerException.class)
                    .hasMessageContaining("found unknown escape character " + LETTER_S_WITH_ASCII_CODE);
        }
    }
}
