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

import org.onap.validation.yaml.error.SchemaValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.schema.YamlSchema;
import org.onap.validation.yaml.schema.node.YamlSchemaNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class YamlValidationProcess {

    private final Queue<YamlValidationStep> validationSteps;
    private final List<SchemaValidationError> errors;
    private final YamlSchema schema;
    private final YamlDocument document;

    public YamlValidationProcess(YamlSchema schema, YamlDocument document) {
        this.schema = schema;
        this.document = document;
        errors = new ArrayList<>();
        validationSteps = new LinkedList<>();
    }

    public List<SchemaValidationError> validate() throws YamlProcessingException {
        validationSteps.add(new YamlValidationStep(schema.getRootNodes(), document));
        while (!validationSteps.isEmpty()) {
            YamlValidationStep nextValidationNode = validationSteps.poll();
            validateStep(nextValidationNode);
        }
        return errors;
    }

    private void validateStep(YamlValidationStep validationNode)
        throws YamlProcessingException {
        for (YamlSchemaNode schemaNode : validationNode.getSchemaNodes()) {
            validateNode(validationNode.getDocument(), schemaNode);
        }
    }

    private void validateNode(YamlDocument document, YamlSchemaNode schemaNode)
        throws YamlProcessingException {

        if (document.containsKey(schemaNode.getName())) {
            if (schemaNode.isContainingSubStructure()) {
                addNextLevelNodeToValidationNodesQueue(document, schemaNode);
            } else if (!isValueOfNodeInAcceptedValuesList(document, schemaNode)) {
                addIncorrectValueError(document, schemaNode);
            }
        } else if (schemaNode.isRequired()) {
            addRequiredKeyNotFoundError(schemaNode);
        }
    }

    private boolean isValueOfNodeInAcceptedValuesList(YamlDocument document, YamlSchemaNode node) {
        return node.getAcceptedValues().isEmpty() ||
            node.getAcceptedValues().containsAll(
                document.getListOfValues(node.getName()).getParameters()
            );
    }

    private void addNextLevelNodeToValidationNodesQueue(YamlDocument document, YamlSchemaNode node)
        throws YamlProcessingException {
        validationSteps.add(
            new YamlValidationStep(
                node.getNextNodes(),
                document.getSubStructure(node.getName())
            )
        );
    }

    private void addRequiredKeyNotFoundError(YamlSchemaNode node) {
        errors.add(
            new SchemaValidationError(
                node.getPath(),
                String.format("Key not found: %s", node.getName())
            )
        );
    }

    private void addIncorrectValueError(YamlDocument document, YamlSchemaNode node) {
        errors.add(
            new SchemaValidationError(
                node.getPath() + node.getName(),
                String.format(
                    "Value(s) is/are not in array of accepted values.%n value(s):  %s%n  accepted value(s):  %s",
                    document.getValue(node.getName()), node.getAcceptedValues())
            )
        );
    }
}
