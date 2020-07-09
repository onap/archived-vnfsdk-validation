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

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.onap.validation.yaml.error.SchemaValidationError;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YamlFileValidatorTest {

    @Test
    public void shouldReturnNoErrorsWhenGivenPathToValidPmDictionaryFile() throws YamlProcessingException {
        // given
        String path = getFullPathForGivenResources(YamlLoadingUtils.PATH_TO_VALID_YAML);

        // when
        List<YamlDocumentValidationError> validationErrors =
            new YamlFileValidator().validateYamlFileWithSchema(path);

        // then
        assertThat(validationErrors).isNotNull();
        assertThat(validationErrors).hasSize(3);
        assertThat(validationErrors).usingRecursiveFieldByFieldElementComparator().containsAll(
            Lists.list(
                new YamlDocumentValidationError(1 ,
                    new SchemaValidationError("/pmMetaData/pmFields/measResultType" ,
                        "Value is not in array of accepted values.\n" +
                            " value:  integer\n" +
                            "  accepted values:  [float, uint32, uint64]")),
                new YamlDocumentValidationError(1 ,
                    new SchemaValidationError("/pmMetaData/pmFields/",
                        "Key not found: measChangeType")),
                new YamlDocumentValidationError(2 ,
                    new SchemaValidationError("/pmMetaData/pmFields/",
                        "Key not found: measChangeType"))
            )
        );
    }

    @Test
    public void shouldThrowErrorWhenGivenPathToInvalidPmDictionaryFile() {
        // given
        String path = getFullPathForGivenResources(YamlLoadingUtils.PATH_TO_MULTI_DOCUMENT_INVALID_YAML);

        // when  then
        assertThatThrownBy(() ->
            new YamlFileValidator().validateYamlFileWithSchema(path)
        ).isInstanceOf(ParserException.class)
            .hasMessageContaining(
                "expected the node content, but found DocumentEnd"
            );
    }

    @Test
    public void shouldThrowErrorWhenGivenInvalidPath() {
        // given
        String path ="invalid/path/to/pm_dictionary";

        // when  then
        assertThatThrownBy(() ->
            new YamlFileValidator().validateYamlFileWithSchema(path)
        ).isInstanceOf(YamlProcessingException.class)
            .hasMessageContaining(
                "PM_Dictionary YAML file is empty"
            );
    }

    private String getFullPathForGivenResources(String pathToValidYaml) {
        return this.getClass().getClassLoader().getResource(
            pathToValidYaml
        ).getPath();
    }
}
