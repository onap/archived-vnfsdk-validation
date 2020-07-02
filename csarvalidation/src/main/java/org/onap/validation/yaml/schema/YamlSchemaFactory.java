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

import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.onap.validation.yaml.schema.node.YamlSchemaNode;
import org.onap.validation.yaml.schema.node.YamlSchemaNodeFactory;

import java.util.ArrayList;
import java.util.List;

import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;
import static org.onap.validation.yaml.model.YamlParameterListFactory.YamlParameterListParsingException;

public class YamlSchemaFactory {


    public YamlSchema createTreeStructuredYamlSchema(YamlDocument schema)
        throws YamlDocumentParsingException, YamlParameterListParsingException {

        return new YamlSchema(getRootNodes(schema));
    }

    private List<YamlSchemaNode> getRootNodes(YamlDocument yamlDocument)
        throws YamlDocumentParsingException, YamlParameterListParsingException {

        List<YamlSchemaNode> nextNodes = new ArrayList<>();
        for(String nodeName: yamlDocument.getYaml().keySet()) {
            nextNodes.add(
                new YamlSchemaNodeFactory().createNode(
                    nodeName,
                    "/",
                    new YamlDocumentFactory().createYamlDocument(
                        yamlDocument.getYaml().get(nodeName)
                    )
                )
            );
        }
        return nextNodes;
    }

}
