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

package org.onap.validation.yaml.model;

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;
import static org.onap.validation.yaml.model.YamlParameterListFactory.YamlParameterListParsingException;

public class YamlDocumentFactoryTest {

    @Test
    public void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToReturnStringifyValues()
        throws YamlDocumentParsingException {
        // given
        Map<Object, Object> inputMap = new HashMap<>();
        List<String> testList = Lists.list("element1", "element11");
        Map<Object, Object> testEmptyMap = Collections.emptyMap();

        inputMap.put("test", testList);
        inputMap.put(345, "element2");
        inputMap.put("test2", "element3");
        inputMap.put(2.67, testEmptyMap);

        // when
        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // then
        assertThat(document).isNotNull();
        assertThat(document.getYaml()).containsKeys("test", "345", "test2", "2.67");

        assertThat((Map<String, Object>)document.getYaml()).containsEntry("test",testList);
        assertThat(document.getValue("test")).isEqualTo("[element1, element11]");

        assertThat(document.getValue("345")).isEqualTo("element2");
        assertThat(document.getValue("test2")).isEqualTo("element3");

        assertThat((Map<String, Object>)document.getYaml()).containsEntry("2.67",testEmptyMap);
        assertThat(document.getValue("2.67")).isEqualTo("{}");
    }

    @Test
    public void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToExtractSubStructure()
        throws YamlDocumentParsingException {
        // given
        Map<Object, Object> inputMap = new HashMap<>();
        Map<Object, Object> subStructureMap = new HashMap<>();

        inputMap.put("test", "element1");
        inputMap.put("structure", subStructureMap);

        subStructureMap.put("subTest1", "subElement1");
        subStructureMap.put("subTest2", "subElement2");

        // when
        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // then
        assertThat(document).isNotNull();
        assertThat(document.getYaml()).containsKeys("test", "structure");
        assertThat(document.getValue("test")).isEqualTo("element1");

        assertThat(document.getSubStructure("structure")).isNotNull();
        assertThat(document.getSubStructure("structure").getValue("subTest1")).isEqualTo("subElement1");
        assertThat(document.getSubStructure("structure").getValue("subTest2")).isEqualTo("subElement2");
    }

    @Test
    public void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToExtractParametersList()
        throws YamlDocumentParsingException, YamlParameterListParsingException {
        // given
        Map<Object, Object> inputMap = new HashMap<>();
        List<String> parametersList = new LinkedList<>();

        inputMap.put("test", "element1");
        inputMap.put("parameters", parametersList);

        parametersList.add("parameter1");
        parametersList.add("parameter2");

        // when
        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // then
        assertThat(document).isNotNull();
        assertThat(document.getYaml()).containsKeys("test", "parameters");
        assertThat(document.getValue("test")).isEqualTo("element1");

        assertThat(document.getListOfValues("parameters")).isNotNull();
        assertThat(document.getListOfValues("parameters").getParameters()).contains("parameter1","parameter2");
    }

    @Test
    public void shouldThrowExceptionIfGetSubStructureIsCalledOnList()
        throws YamlDocumentParsingException {
        // given
        Map<Object, Object> inputMap = new HashMap<>();
        List<String> testList = Lists.list("element1", "element2");

        inputMap.put("test", testList);

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // when then
        assertThatThrownBy(() ->
            document.getSubStructure("test")
        ).isInstanceOf(YamlDocumentParsingException.class)
            .hasMessageContaining(
                String.format("Fail to parse given objects: %s as yaml document", testList)
            );
    }

    @Test
    public void shouldThrowExceptionIfGetSubStructureIsCalledOnString()
        throws YamlDocumentParsingException {
        // given
        Map<Object, Object> inputMap = new HashMap<>();

        inputMap.put("test", "testElement");

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // when then
        assertThatThrownBy(() ->
            document.getSubStructure("test")
        ).isInstanceOf(YamlDocumentParsingException.class)
            .hasMessageContaining(
                String.format("Fail to parse given objects: %s as yaml document.", "testElement")
            );
    }
}
