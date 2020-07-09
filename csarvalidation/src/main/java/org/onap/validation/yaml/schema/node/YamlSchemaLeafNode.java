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

import org.onap.validation.yaml.model.YamlParametersList;

import java.util.Collections;
import java.util.List;

public class YamlSchemaLeafNode extends YamlSchemaNode {

    private final YamlParametersList acceptedValues;

    YamlSchemaLeafNode(String name, String path, boolean required, String comment,
                       YamlParametersList acceptedValues) {
        super(name, path, required, comment);
        this.acceptedValues = acceptedValues;
    }

    @Override
    public List<String> getAcceptedValues() {
        return acceptedValues.getParameters();
    }

    @Override
    public List<YamlSchemaNode> getNextNodes() {
        return Collections.emptyList();
    }

    @Override
    public boolean isContainingSubStructure() {
        return false;
    }

}
