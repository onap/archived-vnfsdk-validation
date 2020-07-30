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

import org.onap.validation.yaml.exception.YamlProcessingException;

import java.util.HashMap;
import java.util.Map;

public class YamlDocumentFactory {

    public YamlDocument createYamlDocument(Object yaml) throws YamlDocumentParsingException {
        try {
            Map<String, Object> parsedYaml = transformMap((Map) yaml);
            return new YamlDocument(parsedYaml);
        } catch (ClassCastException e) {
            throw new YamlDocumentParsingException(
                String.format("Fail to parse given objects: %s as yaml document.", yaml), e
            );
        }
    }

    private Map<String, Object> transformMap(Map<Object, Object> yaml) {
        Map<String, Object> parsedYaml = new HashMap<>();
        for (Map.Entry<Object, Object> entry: yaml.entrySet()) {
            parsedYaml.put(entry.getKey().toString(), entry.getValue());
        }
        return parsedYaml;
    }

    public static class YamlDocumentParsingException extends YamlProcessingException {
        YamlDocumentParsingException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

}
