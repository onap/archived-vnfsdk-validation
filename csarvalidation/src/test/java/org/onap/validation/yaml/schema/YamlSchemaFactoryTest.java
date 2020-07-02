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

package org.onap.validation.yaml.schema;

import org.junit.Test;
import org.onap.validation.yaml.YamlLoadingUtils;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.schema.node.YamlSchemaNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;
import static org.onap.validation.yaml.schema.node.YamlSchemaNodeFactory.EMPTY_COMMENT;
import static org.onap.validation.yaml.schema.node.YamlSchemaNodeFactoryTest.assertThatBranchNodeIsValid;
import static org.onap.validation.yaml.schema.node.YamlSchemaNodeFactoryTest.assertThatLeafNodeIsValid;


public class YamlSchemaFactoryTest {

    @Test
    public void shouldCreateYamlSchemaFromYamlDocumentWithMultipleRoots()
        throws YamlProcessingException {

        // given
        YamlDocument documents = YamlLoadingUtils.loadSimpleValidYamlSchemaWithMultiRootFile();

        // when
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents);

        // then
        assertThat(schema).isNotNull();
        assertThat(schema.getRootNodes()).hasSize(3);
        assertThat(schema.getRootNodes().get(0).getName()).isEqualTo("root1");
        assertThat(schema.getRootNodes().get(1).getName()).isEqualTo("root2");
        assertThat(schema.getRootNodes().get(2).getName()).isEqualTo("root3");
    }


    @Test
    public void shouldCreateYamlSchemaFromYamlDocument()
        throws YamlProcessingException {

        // given
        YamlDocument documents = YamlLoadingUtils.loadSimpleValidYamlSchemaFile();

        // when
        YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents);

        // then
        assertThat(schema).isNotNull();
        assertThat(schema.getRootNodes()).hasSize(1);
        YamlSchemaNode pmMetaData = schema.getRootNodes().get(0);
        assertThatBranchNodeIsValid(pmMetaData, "pmMetaData","/", true, EMPTY_COMMENT,
            2);

        YamlSchemaNode pmHeader = pmMetaData.getNextNodes().get(1);
        assertThatBranchNodeIsValid(pmHeader, "pmHeader","/pmMetaData/", true, EMPTY_COMMENT,
            1);

        YamlSchemaNode nfType = pmHeader.getNextNodes().get(0);
        assertThatLeafNodeIsValid(nfType, "nfType", "/pmMetaData/pmHeader/", true, "nfType comment");

        YamlSchemaNode pmFields = pmMetaData.getNextNodes().get(0);
        assertThatBranchNodeIsValid(pmFields, "pmFields", "/pmMetaData/", true, EMPTY_COMMENT,
            2);

        YamlSchemaNode measChangeType = pmFields.getNextNodes().get(1);
        assertThatLeafNodeIsValid(measChangeType, "measChangeType", "/pmMetaData/pmFields/",
            true, "measChangeType comment",
            "added", "modified", "deleted");

        YamlSchemaNode measAdditionalFields = pmFields.getNextNodes().get(0);
        assertThatBranchNodeIsValid(measAdditionalFields, "measAdditionalFields", "/pmMetaData/pmFields/",
            true, "measAdditionalFields comment",
            2);

        YamlSchemaNode vendorField1 = measAdditionalFields.getNextNodes().get(0);
        assertThatLeafNodeIsValid(vendorField1, "vendorField1", "/pmMetaData/pmFields/measAdditionalFields/",
            true, "vendorField1 comment",
            "X", "Y", "Z");
        YamlSchemaNode vendorField2 = measAdditionalFields.getNextNodes().get(1);
        assertThatLeafNodeIsValid(vendorField2, "vendorField2", "/pmMetaData/pmFields/measAdditionalFields/",
            false, "vendorField2 comment",
            "A", "B");
    }

    @Test
    public void shouldThrowYamlParsingExceptionWhenLoadedSchemaIsInvalid()
        throws YamlDocumentParsingException {

        // given
        YamlDocument documents = YamlLoadingUtils.loadSimpleInvalidYamlSchemaFile();

        // when/then
        assertThatThrownBy(() ->
            new YamlSchemaFactory().createTreeStructuredYamlSchema(documents)
        ).isInstanceOf(YamlDocumentParsingException.class)
            .hasMessageContaining(
                String.format(
                    "Fail to parse given objects: %s as yaml document",
                    documents.getSubStructure("pmMetaData").getYaml().get("structure")
                )
            );
    }

}
