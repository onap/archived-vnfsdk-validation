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

package org.onap.validation.yaml.schema.node;

import org.junit.jupiter.api.Test;
import org.onap.validation.yaml.YamlLoadingUtils;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.schema.node.YamlSchemaNodeFactory.EMPTY_COMMENT;

public class YamlSchemaNodeFactoryTest {

    private static final String ROOT_PATH = "/";

    @Test
    void shouldThrowExceptionDuringLazyLoadingWhenLoadedSchemaHaveInvalidSubStructure()
            throws YamlProcessingException {
        // given
        String nodeName = "pmMetaData";

        YamlDocument document = YamlLoadingUtils.loadSimpleInvalidYamlSchemaForLazyLoadingFile();
        YamlSchemaNode node = new YamlSchemaNodeFactory()
                .createNode(nodeName, ROOT_PATH, document.getSubStructure(nodeName));

        // when/then
        assertThatThrownBy(node::getNextNodes)
                .isInstanceOf(YamlSchemaNode.YamlSchemaProcessingException.class)
                .hasMessageContaining("Lazy loading failed, due to yaml parsing exception.");
    }

    @Test
    void shouldCreateLeafNodeIfGivenYamlDocumentHaveNoSubStructure()
            throws YamlProcessingException {
        // given
        String nodeName = "leaf_test";
        String comment = "test leaf node";
        List<String> acceptedValues = List.of("val1", "val2");
        Map<Object, Object> nodeInYamlFormat = new HashMap<>();
        nodeInYamlFormat.put(YamlSchemaNodeFactory.PRESENCE_KEY, YamlSchemaNodeFactory.PRESENCE_REQUIRED_KEY);
        nodeInYamlFormat.put(YamlSchemaNodeFactory.COMMENT_KEY, comment);
        nodeInYamlFormat.put(YamlSchemaNodeFactory.VALUE_KET, acceptedValues);
        YamlDocument document = new YamlDocumentFactory().createYamlDocument(nodeInYamlFormat);

        // when
        YamlSchemaNode yamlSchemaNode = new YamlSchemaNodeFactory().createNode(nodeName, ROOT_PATH, document);

        // then
        assertThatLeafNodeIsValid(
                yamlSchemaNode, nodeName, ROOT_PATH, true, comment,
                acceptedValues.toArray(new String[acceptedValues.size()])
        );
    }

    @Test
    void shouldCreateBranchNodeIfGivenYamlDocumentHaveSubStructure()
            throws YamlProcessingException {
        // given
        String nodeName = "branch_test";
        String comment = "test branch node";

        Map<Object, Object> subStructure = new HashMap<>();
        String subNode1Name = "branch_test_node1";
        String subNode2Name = "branch_test_node2";
        subStructure.put(subNode1Name, new HashMap<>());
        subStructure.put(subNode2Name, new HashMap<>());

        Map<Object, Object> nodeInYamlFormat = new HashMap<>();
        nodeInYamlFormat.put(YamlSchemaNodeFactory.PRESENCE_KEY, YamlSchemaNodeFactory.PRESENCE_REQUIRED_KEY);
        nodeInYamlFormat.put(YamlSchemaNodeFactory.COMMENT_KEY, comment);
        nodeInYamlFormat.put(YamlSchemaNodeFactory.STRUCTURE_KEY, subStructure);
        YamlDocument document = new YamlDocumentFactory().createYamlDocument(nodeInYamlFormat);

        // when
        YamlSchemaNode yamlSchemaNode = new YamlSchemaNodeFactory().createNode(nodeName, ROOT_PATH, document);

        // then
        assertThatBranchNodeIsValid(yamlSchemaNode, nodeName, ROOT_PATH, true, comment, 2);

        List<YamlSchemaNode> subNodes = yamlSchemaNode.getNextNodes();
        assertThat(subNodes).hasSize(2);
        assertThatLeafNodeIsValid(
                subNodes.get(1), subNode1Name, ROOT_PATH + nodeName + "/", false, EMPTY_COMMENT);
        assertThatLeafNodeIsValid(
                subNodes.get(0), subNode2Name, ROOT_PATH + nodeName + "/", false, EMPTY_COMMENT);
    }

    public static void assertThatBranchNodeIsValid(
            YamlSchemaNode yamlSchemaNode, String name, String path, boolean isRequired, String comment,
            int numberOfSubNodes
    ) throws YamlSchemaNode.YamlSchemaProcessingException {
        assertThatNodeIsValid(yamlSchemaNode, name, path, isRequired, comment);

        assertThat(yamlSchemaNode.getClass()).isEqualTo(YamlSchemaBranchNode.class);
        assertThat(yamlSchemaNode.isContainingSubStructure()).isTrue();
        assertThat(yamlSchemaNode.getNextNodes()).hasSize(numberOfSubNodes);
        assertThat(yamlSchemaNode.getAcceptedValues()).isEmpty();
    }

    public static void assertThatLeafNodeIsValid(
            YamlSchemaNode yamlSchemaNode, String name, String path, boolean isRequired, String comment,
            String... acceptedValues
    ) throws YamlSchemaNode.YamlSchemaProcessingException {
        assertThatNodeIsValid(yamlSchemaNode, name, path, isRequired, comment);

        assertThat(yamlSchemaNode.getClass()).isEqualTo(YamlSchemaLeafNode.class);
        assertThat(yamlSchemaNode.isContainingSubStructure()).isFalse();
        assertThat(yamlSchemaNode.getAcceptedValues()).containsExactly(acceptedValues);
        assertThat(yamlSchemaNode.getNextNodes()).isEmpty();
    }

    private static void assertThatNodeIsValid(YamlSchemaNode yamlSchemaNode, String name, String path, boolean isRequired, String comment) {
        assertThat(yamlSchemaNode).isNotNull();
        assertThat(yamlSchemaNode.getName()).isEqualTo(name);
        assertThat(yamlSchemaNode.getPath()).isEqualTo(path);
        if (comment.isEmpty()) {
            assertThat(yamlSchemaNode.getComment()).isNotEmpty();
        } else {
            assertThat(yamlSchemaNode.getComment()).isEqualTo(comment);
        }
        assertThat(yamlSchemaNode.isRequired()).isEqualTo(isRequired);
    }
}
