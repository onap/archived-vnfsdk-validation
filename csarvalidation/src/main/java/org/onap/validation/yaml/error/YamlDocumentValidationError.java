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

package org.onap.validation.yaml.error;

public class YamlDocumentValidationError {
    private final int yamlDocumentNumber;
    private final SchemaValidationError schemaValidationErrors;

    public YamlDocumentValidationError(int yamlDocumentNumber, SchemaValidationError schemaValidationErrors) {
        this.yamlDocumentNumber = yamlDocumentNumber;
        this.schemaValidationErrors = schemaValidationErrors;
    }

    public int getYamlDocumentNumber() {
        return yamlDocumentNumber;
    }

    public String getPath() {
        return  schemaValidationErrors.getPath();
    }

    public String getMessage() {
        return  schemaValidationErrors.getMessage();
    }

}
