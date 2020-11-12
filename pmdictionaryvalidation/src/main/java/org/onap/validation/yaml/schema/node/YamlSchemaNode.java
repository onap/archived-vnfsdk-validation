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

import java.util.List;

public abstract class YamlSchemaNode {

    private final String path;
    private final String name;
    private final boolean required;
    private final String comment;


    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isRequired() {
        return required;
    }

    public abstract List<String> getAcceptedValues();

    public abstract List<YamlSchemaNode> getNextNodes() throws YamlSchemaProcessingException;

    public abstract boolean isContainingSubStructure();

    public String getComment() {
        return comment;
    }

    YamlSchemaNode(String name, String path, boolean required, String comment) {
        this.name = name;
        this.path = path;
        this.required = required;
        this.comment = comment;
    }

    static class YamlSchemaProcessingException extends YamlProcessingException {
        YamlSchemaProcessingException(Throwable throwable) {
            super(throwable);
        }
    }
}
