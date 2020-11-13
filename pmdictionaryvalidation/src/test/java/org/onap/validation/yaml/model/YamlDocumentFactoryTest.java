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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.yaml.model.YamlDocumentFactory.YamlDocumentParsingException;

class YamlDocumentFactoryTest {

    @Test
    void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToReturnStringifyValues()
            throws YamlDocumentParsingException {
        List<String> testList = List.of("element1", "element11");
        Map<Object, Object> testEmptyMap = Collections.emptyMap();
        Map<Object, Object> inputMap = Map.of(
                "test", testList,
                345, "element2",
                "test2", "element3",
                2.67, testEmptyMap);

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        assertYamlDocument(document, inputMap);
    }

    @Test
    void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToExtractSubStructure()
            throws YamlDocumentParsingException {
        Map<Object, Object> subStructureMap = Map.of(
                "subTest1", "subElement1",
                "subTest2", "subElement2");
        Map<Object, Object> inputMap = Map.of(
                "test", "element1",
                "structure", subStructureMap);

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        assertYamlDocument(document, inputMap);
    }

    @Test
    void shouldTurnMapOfUnknownKeyTypeToMapWithStringKeysAndBeAbleToExtractParametersList()
            throws YamlDocumentParsingException {
        List<String> parametersList = List.of("parameter1", "parameter2");
        Map<Object, Object> inputMap = Map.of(
                "test", "element1",
                "parameters", parametersList);

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        assertYamlDocument(document, inputMap);
    }

    @Test
    void shouldThrowExceptionIfGetSubStructureIsCalledOnList()
            throws YamlDocumentParsingException {
        // given
        List<String> testList = List.of("element1", "element2");
        Map<Object, Object> inputMap = Collections.singletonMap("test", testList);

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // when then
        assertYamlDocumentParsingException(document, testList);
    }

    @Test
    void shouldThrowExceptionIfGetSubStructureIsCalledOnString()
            throws YamlDocumentParsingException {
        // given
        Map<Object, Object> inputMap = Collections.singletonMap("test", "testElement");

        YamlDocument document = new YamlDocumentFactory().createYamlDocument(inputMap);

        // when then
        assertYamlDocumentParsingException(document, "testElement");
    }

    private void assertYamlDocument(YamlDocument document, Map<Object, Object> inputMap) {
        assertThat(document).isNotNull();
        assertThat(document.getYaml()).containsExactlyEntriesOf(mapKeyToString(inputMap));
    }

    private Map<String, Object> mapKeyToString(Map<Object, Object> inputMap) {
        return inputMap.entrySet().stream()
                .collect(Collectors.toMap(w -> w.getKey().toString(), Map.Entry::getValue));
    }

    private void assertYamlDocumentParsingException(YamlDocument document, Object unparsed) {
        assertThatThrownBy(() -> document.getSubStructure("test"))
                .isInstanceOf(YamlDocumentParsingException.class)
                .hasMessageContaining(String.format("Fail to parse given objects: %s as yaml document.", unparsed));
    }
}
