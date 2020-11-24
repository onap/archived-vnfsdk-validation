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

import static org.assertj.core.api.Assertions.assertThat;

class YamlParameterListFactoryTest {

    @Test
    void shouldCreateEmptyParametersList() {
        // when
        YamlParametersList parametersList = new YamlParameterListFactory().createEmptyYamlParameterList();

        // then
        assertThat(parametersList).isNotNull();
        assertThat(parametersList.getParameters()).isEmpty();
    }

    @Test
    void shouldCreateParametersListContainingStringsFromListContainingSimpleTypes() {
        List<Object> testList = List.of("test1", 3, 23.45, 'a', "test2");

        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList(testList);

        assertYamlParametersList(parametersList, testList);
    }

    @Test
    void shouldCreateParametersListContainingStringsFromListContainingVariousTypes() {
        List<Object> testList = List.of("test1", 3, List.of(2, 3, 4), "test2");

        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList(testList);

        assertYamlParametersList(parametersList, testList);
    }

    @Test
    void shouldCreateListWithOneStringWhenGivenObjectIsNotList() {
        Object testObject = "test";

        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList(testObject);

        assertYamlParametersList(parametersList, Collections.singletonList(testObject));
    }

    private void assertYamlParametersList(YamlParametersList parametersList, List<Object> testList) {
        assertThat(parametersList).isNotNull();
        assertThat(parametersList.getParameters())
                .containsExactly(mapToStrings(testList));
    }

    private String[] mapToStrings(List<Object> elements) {
        return elements.stream().map(Object::toString).toArray(String[]::new);
    }

}
