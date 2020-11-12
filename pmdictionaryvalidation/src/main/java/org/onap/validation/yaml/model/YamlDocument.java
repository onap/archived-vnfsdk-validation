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

package org.onap.validation.yaml.model;

import java.util.Map;

public class YamlDocument {

    private final Map<String, Object> yaml;

    YamlDocument(Map<String, Object> yaml) {
        this.yaml = yaml;
    }

    public Map<String, Object> getYaml() {
        return yaml;
    }

    public boolean containsKey(String key) {
        return yaml.containsKey(key);
    }

    public String getValue(String key) {
        return yaml.get(key).toString();
    }

    public YamlParametersList getListOfValues(String key) {
        return new YamlParameterListFactory().createYamlParameterList(
            yaml.get(key)
        );
    }

    public YamlDocument getSubStructure(String name)
        throws YamlDocumentFactory.YamlDocumentParsingException {
        return new YamlDocumentFactory().createYamlDocument(
            yaml.get(name)
        );
    }
}


