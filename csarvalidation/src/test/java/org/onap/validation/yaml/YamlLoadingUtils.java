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

import java.net.URL;
import java.util.List;

import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;

public final class YamlLoadingUtils {

    private YamlLoadingUtils() {}

    private static final String PATH_TO_VALID_YAML = "yaml_schema/PM_Dictionary.yaml";
    private static final String PATH_TO_SIMPLE_VALID_SCHEMA = "yaml_schema/Simple_Valid_Schema.yaml";
    private static final String PATH_TO_SIMPLE_VALID_SCHEMA_MULTI_ROOT = "yaml_schema/Simple_Valid_Schema_Multi_Root.yaml";
    private static final String PATH_TO_SIMPLE_INVALID_SCHEMA = "yaml_schema/Simple_Invalid_Schema_Construction.yaml";
    private static final String PATH_TO_SIMPLE_INVALID_SCHEMA_FOR_LAZY_LOADING = "yaml_schema/Simple_Invalid_Schema_LazyLoading.yaml";

    public static List<YamlDocument> loadValidMultiDocumentYamlFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_VALID_YAML));
    }

    public static YamlDocument loadSimpleValidYamlSchemaFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_SIMPLE_VALID_SCHEMA)).get(0);
    }

    public static YamlDocument loadSimpleInvalidYamlSchemaFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_SIMPLE_INVALID_SCHEMA)).get(0);
    }

    public static YamlDocument loadSimpleInvalidYamlSchemaForLazyLoadingFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_SIMPLE_INVALID_SCHEMA_FOR_LAZY_LOADING)).get(0);
    }

    public static YamlDocument loadSimpleValidYamlSchemaWithMultiRootFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_SIMPLE_VALID_SCHEMA_MULTI_ROOT)).get(0);
    }

    private static URL getUrlForGivenPath(String path) {
        return YamlLoadingUtils.class.getClassLoader().getResource(path);
    }
}
