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

import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YamlLoader {

    public List<YamlDocument> loadMultiDocumentYamlFile(URL path)
        throws YamlDocumentFactory.YamlDocumentParsingException {
        List<YamlDocument> documentsFromFile = new ArrayList<>();
        try (InputStream yamlStream = path.openStream()) {
            for (Object yamlDocument : new Yaml().loadAll(yamlStream)) {
                documentsFromFile.add(
                    new YamlDocumentFactory().createYamlDocument(yamlDocument)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documentsFromFile;
    }

}
