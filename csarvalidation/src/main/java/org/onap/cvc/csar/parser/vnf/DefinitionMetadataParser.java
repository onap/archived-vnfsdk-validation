/*
 * Copyright 2019 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.cvc.csar.parser.vnf;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.CSARArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Code extracted from CSARArchive class.
 */
public class DefinitionMetadataParser {

    private static final String[] ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSIONS = {
            CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION_SIMPLE_1_0,
            CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION_SIMPLE_1_1,
            CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION_SIMPLE_1_2
    };

    private Map<String, ?> yaml;
    private String definitionYamlFileName;
    private CSARArchive.Mode mode;

    public DefinitionMetadataParser(Map<String, ?> yaml, String definitionYamlFileName, CSARArchive.Mode mode) {
        this.yaml = yaml;
        this.definitionYamlFileName = definitionYamlFileName;
        this.mode = mode;
    }

    public Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> parse(){
        List<CSARArchive.CSARError> errors = new ArrayList<>();
        CSARArchive.Definition definition = new CSARArchive.Definition();

        //yaml is empty or version string missing
        if (yaml == null || !yaml.containsKey(CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION)) {
            errors.add(
                    new CSARArchive.CSARErrorEntryMissingToscaDefinitionVersion(
                            this.definitionYamlFileName));
        } else {
            convertYamlIntoDefinitionModel(yaml, definition, errors);
        }
        return Pair.of(definition,errors);
    }

    private void convertYamlIntoDefinitionModel(Map<String, ?> yaml, CSARArchive.Definition definition, List<CSARArchive.CSARError> errors) {
        String version = (String) yaml.get(CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION);
        if (!Arrays.asList(ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSIONS).contains(version)) {
            errors.add(new CSARArchive.CSARErrorInvalidEntry(CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION,
                    this.definitionYamlFileName, -1, "Should be " + CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION_SIMPLE_1_1));
        } else {
            updateDefinitionModel(definition, version, errors);
        }
    }

    private void updateDefinitionModel(CSARArchive.Definition definition, String version, List<CSARArchive.CSARError> errors) {
        definition.setToscaDefinitionVersion(version);

        if (mode.equals(CSARArchive.Mode.WITHOUT_TOSCA_META_DIR)) {
            //metadata section should be there
            if (!yaml.containsKey(CSARArchive.ENTRY_DEFINITION_METADATA)) {
                errors.add(
                        new CSARArchive.CSARErrorInvalidEntryValueToscaDefinitionVersion(
                                this.definitionYamlFileName));
            } else {
                Map<String, String> metadata = (Map<String, String>) yaml.get(CSARArchive.ENTRY_DEFINITION_METADATA);
                final CSARArchive.Definition.Metadata metadataModel = definition.getMetadata();
                populateMetadataModel(metadata, errors, metadataModel);
                validateMetadataModel(errors, metadataModel);
            }
        }
    }

    private void populateMetadataModel(Map<String, String> metadata, List<CSARArchive.CSARError> errors, CSARArchive.Definition.Metadata metadataModel) {

        for(Map.Entry<String, String> entry: metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            //continue till it reaches the metadata section
            if (key.equalsIgnoreCase(CSARArchive.ENTRY_DEFINITION_TEMPLATE_AUTHOR)) {
                metadataModel.setTemplateAuthor(value);
            } else if (key.equalsIgnoreCase(CSARArchive.ENTRY_DEFINITION_TEMPLATE_NAME)) {
                metadataModel.setTempalteName(value);
            } else if (key.equalsIgnoreCase(CSARArchive.ENTRY_DEFINITION_TEMPLATE_VERSION)) {
                metadataModel.setTemplateVersion(value);
            } else {
                errors.add(
                        new CSARArchive.CSARErrorIgnored(
                                key,
                                this.definitionYamlFileName,
                                -1,
                                null));
            }
        }
    }

    private void validateMetadataModel(List<CSARArchive.CSARError> errors, CSARArchive.Definition.Metadata metadataModel) {
        if (metadataModel.getTemplateAuthor() == null) {
            errors.add(
                    new CSARArchive.CSARErrorEntryMissingToscaDefinitionMetadataTemplateAuthor(
                            this.definitionYamlFileName));
        }
        if (metadataModel.getTemplateName() == null) {
            errors.add(new CSARArchive.CSARErrorEntryMissingToscaDefinitionMetadataTemplateName(
                    this.definitionYamlFileName));
        }

        if (metadataModel.getTemplateVersion() == null) {
            errors.add(new CSARArchive.CSARErrorEntryMissingToscaDefinitionMetadataTemplateVersion(
                    this.definitionYamlFileName));
        }
    }

}
