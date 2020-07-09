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

package org.onap.validation.yaml;

import org.onap.validation.yaml.error.SchemaValidationError;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.schema.YamlSchema;
import org.onap.validation.yaml.schema.YamlSchemaFactory;

import java.util.ArrayList;
import java.util.List;

public class YamlFileValidator {

    public List<YamlDocumentValidationError> validateYamlFileWithSchema(String pathToFile)
        throws YamlProcessingException {

        List<YamlDocumentValidationError> yamlFileValidationErrors = new ArrayList<>();

        List<YamlDocument> documents = new YamlLoader().loadMultiDocumentYamlFile(pathToFile);
        if(!documents.isEmpty()) {

            YamlSchema schema = new YamlSchemaFactory().createTreeStructuredYamlSchema(documents.get(0));
            YamlValidator validator = new YamlValidator(schema);

            for (int index = 1; index < documents.size(); index++) {
                List<SchemaValidationError> validationErrors = validator.validate(documents.get(index));
                for (SchemaValidationError validationError : validationErrors) {
                    yamlFileValidationErrors.add(
                        new YamlDocumentValidationError(index, validationError)
                    );
                }
            }

            return yamlFileValidationErrors;
        } else {
            throw new YamlProcessingException("PM_Dictionary YAML file is empty");
        }
    }

}
