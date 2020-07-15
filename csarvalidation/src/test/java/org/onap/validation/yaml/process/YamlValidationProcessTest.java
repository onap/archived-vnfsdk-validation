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

package org.onap.validation.yaml.process;

import org.junit.Test;
import org.onap.validation.yaml.YamlLoadingUtils;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.error.SchemaValidationError;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.schema.YamlSchema;
import org.onap.validation.yaml.schema.YamlSchemaFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.YamlLoadingUtils.VALID_YAML_DOCUMENT_INDEX;
import static org.onap.validation.yaml.YamlLoadingUtils.YAML_DOCUMENT_WITH_MISSING_FIELD_INDEX;
import static org.onap.validation.yaml.YamlLoadingUtils.YAML_DOCUMENT_WITH_MISSING_FIELD_AND_WRONG_VALUE_INDEX;


public class YamlValidationProcessTest {

    @Test
    public void shouldReturnNoErrorWhenProcessingValidPmDictionaryYaml()
        throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0));
        YamlDocument document = documents.get(VALID_YAML_DOCUMENT_INDEX);

        // when
        List<SchemaValidationError> errors = new YamlValidationProcess(schema,document).validate();

        // then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnOneErrorWhenProcessingPmDictionaryYamlWithMissingField()
        throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0));
        YamlDocument document = documents.get(YAML_DOCUMENT_WITH_MISSING_FIELD_INDEX);

        // when
        List<SchemaValidationError> errors = new YamlValidationProcess(schema,document).validate();

        // then
        assertThat(errors).hasSize(1);
    }

    @Test
    public void shouldReturnTwoErrorsWhenProcessingPmDictionaryYamlWithMissingFieldAndIncorrectValue()
        throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0));
        YamlDocument document = documents.get(YAML_DOCUMENT_WITH_MISSING_FIELD_AND_WRONG_VALUE_INDEX);

        // when
        List<SchemaValidationError> errors = new YamlValidationProcess(schema,document).validate();

        // then
        assertThat(errors).hasSize(2);
    }

    @Test
    public void shouldThrowExceptionWhenProcessingPmDictionaryIsNotValidYaml()
        throws YamlProcessingException {
        // given
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();
        YamlDocument schemaInYaml = YamlLoadingUtils.loadSimpleInvalidYamlSchemaForLazyLoadingFile();
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(schemaInYaml);
        YamlDocument document = documents.get(VALID_YAML_DOCUMENT_INDEX);

        // when then
        assertThatThrownBy(() ->
            new YamlValidationProcess(schema,document).validate()
        ).isInstanceOf(YamlProcessingException.class)
            .hasMessageContaining(
                String.format("Lazy loading failed, due to yaml parsing exception.")
            );
    }
}
