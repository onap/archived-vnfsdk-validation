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

package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.onap.cvc.csar.parser.vnf.DefinitionMetadataParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.CSARArchive.Definition;
import static org.onap.cvc.csar.CSARArchive.ENTRY_DEFINITION_TEMPLATE_AUTHOR;
import static org.onap.cvc.csar.CSARArchive.ENTRY_DEFINITION_TEMPLATE_NAME;
import static org.onap.cvc.csar.CSARArchive.ENTRY_DEFINITION_TEMPLATE_VERSION;
import static org.onap.cvc.csar.CSARArchive.ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;

public class DefinitionMetadataParserTest {

    private static final String DEFINITION_YAML = "definition.yaml";

    @Test
    public void shouldReportThatDefinitionYamlDataAreEmpty(){
        // given
        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                new HashMap<>(), DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        assertThat(convertToMessagesList(data.getRight())).contains(
                "Missing. Entry [tosca_definitions_version]"
        );
    }

    @Test
    public void shouldReportThatDefinitionYamlDataIsInUnsupportedVersion(){
        // given
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_0_0");
        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        assertThat(convertToMessagesList(data.getRight())).contains(
                "Invalid. Entry [tosca_definitions_version]. Should be tosca_simple_yaml_1_1"
        );
    }

    @Test
    public void shouldSetOnlyAVersionWhenMetaIsForToscaDirMode(){
        // given
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_1_1");
        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITH_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        final CSARArchive.Definition definition = data.getLeft();
        final List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(definition.getToscaDefinitionVersion()).isEqualTo("tosca_simple_yaml_1_1");
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReportAnErrorWhenMetaIsNotSet(){
        // given
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_1_1");
        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        final CSARArchive.Definition definition = data.getLeft();
        final List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(definition.getToscaDefinitionVersion()).isEqualTo("tosca_simple_yaml_1_1");
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [tosca_definitions_version]. Valid values are [tosca_simple_yaml_1_1]"
        );
    }

    @Test
    public void shouldPopulateMetadataModel(){
        // given
        final Map<String, Object> metadata = givenPopulateMetadataData();
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_1_1");
        yaml.put("metadata", metadata);

        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        //then
        final CSARArchive.Definition definition = data.getLeft();
        final List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(definition.getToscaDefinitionVersion()).isEqualTo("tosca_simple_yaml_1_1");
        assertThat(convertToMessagesList(errors)).isEmpty();
        final Definition.Metadata metadataModel = definition.getMetadata();
        assertThat(metadataModel.getTemplateAuthor()).isEqualTo("author");
        assertThat(metadataModel.getTemplateName()).isEqualTo("template name");
        assertThat(metadataModel.getTemplateVersion()).isEqualTo("template version");
    }

    @Test
    public void shouldReportAnErrorWhenMetadataParameterIsNotSupported(){
        // given
        final Map<String, Object> metadata = givenPopulateMetadataData();
        metadata.put("NEW ENTRY DEFINITION TEMPLATE PARAMETER", "some value");
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_1_1");
        yaml.put("metadata", metadata);

        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        final List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(convertToMessagesList(errors)).contains(
                "Ignored. Entry [NEW ENTRY DEFINITION TEMPLATE PARAMETER]"
        );
    }

    @Test
    public void shouldReportErrorsWhenMetadataModelIsEmpty(){
        // given
        final Map<String, Object> metadata = new HashMap<>();
        final HashMap<String, Object> yaml = givenEmptyYamlData("tosca_simple_yaml_1_1");
        yaml.put("metadata", metadata);

        DefinitionMetadataParser definitionMetadataParser = new DefinitionMetadataParser(
                yaml, DEFINITION_YAML, CSARArchive.Mode.WITHOUT_TOSCA_META_DIR);

        // when
        final Pair<CSARArchive.Definition, List<CSARArchive.CSARError>> data = definitionMetadataParser.parse();

        // then
        final List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [template_author]",
                "Missing. Entry [template_name]",
                "Missing. Entry [template_version]"
        );
    }


    private HashMap<String, Object> givenEmptyYamlData(String tosca_simple_yaml_0_0) {
        final HashMap<String, Object> yaml = new HashMap<>();
        yaml.put(ENTRY_DEFINITION_TOSCA_DEFINITIONS_VERSION, tosca_simple_yaml_0_0);
        return yaml;
    }

    private Map<String, Object> givenPopulateMetadataData() {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put(ENTRY_DEFINITION_TEMPLATE_AUTHOR, "author");
        metadata.put(ENTRY_DEFINITION_TEMPLATE_NAME, "template name");
        metadata.put(ENTRY_DEFINITION_TEMPLATE_VERSION, "template version");
        return metadata;
    }
}
