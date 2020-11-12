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

import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.onap.validation.yaml.model.YamlParameterListFactory;
import org.onap.validation.yaml.model.YamlParametersList;

import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;

public class YamlSchemaNodeFactory {

    public static final String EMPTY_COMMENT = "no comment available";
    static final String STRUCTURE_KEY = "structure";
    static final String COMMENT_KEY = "comment";
    static final String VALUE_KET = "value";
    static final String PRESENCE_KEY = "presence";
    static final String PRESENCE_REQUIRED_KEY = "required";

    public YamlSchemaNode createNode(String nodeName, String path, YamlDocument yamlDocument)
        throws YamlProcessingException {

        YamlSchemaNode yamlSchemaNode;
        if(isYamlContainingKey(yamlDocument, STRUCTURE_KEY)) {
            yamlSchemaNode = new YamlSchemaBranchNode(
                nodeName, path, getIsPresenceRequired(yamlDocument), getComment(yamlDocument),
                getNextNodes(yamlDocument)
            );
        } else {
            yamlSchemaNode = new YamlSchemaLeafNode(
                nodeName, path, getIsPresenceRequired(yamlDocument), getComment(yamlDocument),
                getAcceptedValues(yamlDocument)
            );
        }
        return  yamlSchemaNode;
    }

    private YamlDocument getNextNodes(YamlDocument yamlDocument)
        throws YamlDocumentParsingException {
        return new YamlDocumentFactory().createYamlDocument(yamlDocument.getYaml().get(STRUCTURE_KEY));
    }

    private String getComment(YamlDocument yamlDocument) {

        return isYamlContainingKey(yamlDocument, COMMENT_KEY)
            ? yamlDocument.getYaml().get(COMMENT_KEY).toString()
            : EMPTY_COMMENT;
    }

    private YamlParametersList getAcceptedValues(YamlDocument yamlDocument) {

        return isYamlContainingKey(yamlDocument, VALUE_KET)
            ? new YamlParameterListFactory().createYamlParameterList(yamlDocument.getYaml().get(VALUE_KET))
            : new YamlParameterListFactory().createEmptyYamlParameterList();
    }

    private boolean getIsPresenceRequired(YamlDocument yamlDocument) {

        return isYamlContainingKey(yamlDocument, PRESENCE_KEY)
            && yamlDocument.getYaml().get(PRESENCE_KEY).equals(PRESENCE_REQUIRED_KEY);
    }

    private boolean isYamlContainingKey(YamlDocument yamlDocument, String structureKey) {
        return yamlDocument.getYaml().containsKey(structureKey);
    }

}
