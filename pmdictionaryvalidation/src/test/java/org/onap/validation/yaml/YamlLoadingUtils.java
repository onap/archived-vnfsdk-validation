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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;

public final class YamlLoadingUtils {

    private YamlLoadingUtils() {
    }

    public static final int VALID_YAML_DOCUMENT_INDEX = 4;
    public static final int YAML_DOCUMENT_WITH_WRONG_VALUE_IN_ARRAY_INDEX = 3;
    public static final int YAML_DOCUMENT_WITH_MISSING_FIELD_INDEX = 2;
    public static final int YAML_DOCUMENT_WITH_MISSING_FIELD_AND_WRONG_VALUE_INDEX = 1;

    public static final String PATH_TO_VALID_YAML = "yaml_schema/PM_Dictionary.yaml";
    public static final String PATH_TO_VALID_JSON_STYLE_YAML = "yaml_schema/PM_Dictionary_JSON_Style.yaml";
    public static final String PATH_TO_SIMPLE_VALID_SCHEMA = "yaml_schema/Simple_Valid_Schema.yaml";
    public static final String PATH_TO_SIMPLE_VALID_SCHEMA_MULTI_ROOT = "yaml_schema/Simple_Valid_Schema_Multi_Root.yaml";
    public static final String PATH_TO_SIMPLE_INVALID_SCHEMA = "yaml_schema/Simple_Invalid_Schema_Construction.yaml";
    public static final String PATH_TO_SIMPLE_INVALID_SCHEMA_FOR_LAZY_LOADING = "yaml_schema/Simple_Invalid_Schema_LazyLoading.yaml";
    public static final String PATH_TO_MULTI_DOCUMENT_INVALID_YAML = "yaml_schema/Multi_Document_Invalid.yaml";
    public static final String PATH_TO_INVALID_YAML_WITH_INCORRECT_KEY_MAPPING = "yaml_schema/Simple_Invalid_Mapping_Value.yaml";
    public static final String PATH_TO_INVALID_YAML_WITH_UNKNOWN_ESCAPE_CHARACTER = "yaml_schema/Simple_Unknown_Escape_Character.yaml";

    public static List<YamlDocument> loadValidMultiDocumentYamlFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_VALID_YAML));
    }

    public static List<YamlDocument> loadValidJsonStyleMultiDocumentYamlFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_VALID_JSON_STYLE_YAML));
    }

    public static List<YamlDocument> loadValidMultiDocumentYamlFileUsingStringPath() throws YamlProcessingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_VALID_YAML).getPath());
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

    public static List<YamlDocument> tryToLoadMultiDocumentInvalidYamlFile() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_MULTI_DOCUMENT_INVALID_YAML));
    }

    public static List<YamlDocument> tryToLoadMultiDocumentInvalidYamlFileUsingStringPath() throws YamlProcessingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_MULTI_DOCUMENT_INVALID_YAML).getPath());
    }

    public static List<YamlDocument> tryToLoadInvalidYamlFileWithIncorrectKeyMapping() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_INVALID_YAML_WITH_INCORRECT_KEY_MAPPING));
    }

    public static List<YamlDocument> tryToLoadInvalidYamlFileWithUnknownEscapeCharacter() throws YamlDocumentParsingException {
        return new YamlLoader().loadMultiDocumentYamlFile(getUrlForGivenPath(PATH_TO_INVALID_YAML_WITH_UNKNOWN_ESCAPE_CHARACTER));
    }

    public static byte[] readFile(String path) throws IOException {
        String file = getUrlForGivenPath(path).getFile();
        return Files.readAllBytes(Path.of(file));
    }

    private static URL getUrlForGivenPath(String path) {
        return YamlLoadingUtils.class.getClassLoader().getResource(path);
    }
}
