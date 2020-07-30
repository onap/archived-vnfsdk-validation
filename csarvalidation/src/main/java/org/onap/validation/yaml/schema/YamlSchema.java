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

import org.onap.validation.yaml.schema.node.YamlSchemaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YamlSchema {

    private final List<YamlSchemaNode> rootNodes;

    public List<YamlSchemaNode> getRootNodes() {
        return Collections.unmodifiableList(rootNodes);
    }

    YamlSchema(List<YamlSchemaNode> rootNodes) {
        this.rootNodes = new ArrayList<>(rootNodes);
    }
}
