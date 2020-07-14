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

import org.junit.Test;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class YamlLoaderTest {

    @Test
    public void shouldLoadAllDocumentsFromYamlFile() throws YamlDocumentFactory.YamlDocumentParsingException {
        // when
        List<YamlDocument> documents = YamlLoadingUtils.loadValidMultiDocumentYamlFile();

        // then
        assertThat(documents.size()).isEqualTo(4);
    }

}