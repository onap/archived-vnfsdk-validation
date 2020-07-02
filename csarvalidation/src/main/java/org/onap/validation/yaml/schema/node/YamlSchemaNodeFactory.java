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

import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.onap.validation.yaml.model.YamlParameterListFactory;
import org.onap.validation.yaml.model.YamlParametersList;

import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;
import static org.onap.validation.yaml.model.YamlParameterListFactory.YamlParameterListParsingException;

public class YamlSchemaNodeFactory {

    public static final String EMPTY_COMMENT = "no comment available";

    public YamlSchemaNode createNode(String nodeName, String path, YamlDocument yamlDocument)
        throws YamlDocumentParsingException, YamlParameterListParsingException {

        YamlSchemaNode yamlSchemaNode;
        if( yamlDocument.getYaml().containsKey("structure")) {
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
        return new YamlDocumentFactory().createYamlDocument(yamlDocument.getYaml().get("structure"));
    }

    private String getComment(YamlDocument yamlDocument) {

        return yamlDocument.getYaml().containsKey("comment")
            ? yamlDocument.getYaml().get("comment").toString()
            : EMPTY_COMMENT;
    }

    private YamlParametersList getAcceptedValues(YamlDocument yamlDocument)
        throws YamlParameterListParsingException {

        return yamlDocument.getYaml().containsKey("value")
            ? new YamlParameterListFactory().createYamlParameterList(yamlDocument.getYaml().get("value"))
            : new YamlParameterListFactory().createEmptyYamlParameterList();
    }

    private boolean getIsPresenceRequired(YamlDocument yamlDocument) {

        return yamlDocument.getYaml().containsKey("presence")
            && yamlDocument.getYaml().get("presence").equals("required");
    }

}
