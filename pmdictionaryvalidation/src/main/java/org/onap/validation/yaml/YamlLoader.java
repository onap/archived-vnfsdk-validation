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

import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class YamlLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlLoader.class);

    List<YamlDocument> loadMultiDocumentYaml(byte[] yamlWithSchema)
            throws YamlDocumentFactory.YamlDocumentParsingException {
        List<YamlDocument> documents = new ArrayList<>();
        try (InputStream yamlStream = new ByteArrayInputStream(yamlWithSchema)) {
            for (Object yamlDocument : new Yaml().loadAll(yamlStream)) {
                documents.add(new YamlDocumentFactory().createYamlDocument(yamlDocument));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load multi document YAML", e);
        }
        return documents;
    }

    List<YamlDocument> loadMultiDocumentYamlFile(URL path)
            throws YamlDocumentFactory.YamlDocumentParsingException {
        List<YamlDocument> documentsFromFile = new ArrayList<>();
        try (InputStream yamlStream = path.openStream()) {
            for (Object yamlDocument : new Yaml().loadAll(yamlStream)) {
                documentsFromFile.add(
                        new YamlDocumentFactory().createYamlDocument(yamlDocument)
                );
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load multi document YAML file", e);
        }
        return documentsFromFile;
    }

    List<YamlDocument> loadMultiDocumentYamlFile(String path)
            throws YamlProcessingException {
        try {
            return loadMultiDocumentYamlFile(new URL("file://" + path));
        } catch (MalformedURLException e) {
            throw new YamlProcessingException("Fail to read file under given path.", e);
        }
    }
}
