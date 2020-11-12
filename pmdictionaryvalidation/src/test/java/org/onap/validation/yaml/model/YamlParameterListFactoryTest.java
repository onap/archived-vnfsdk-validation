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
import org.junit.jupiter.api.Test;

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
        // given
        List<Object>  elements = List.of("test1", 3, 23.45, 'a', "test2");

        // when
        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList( elements);

        // then
        assertThat(parametersList).isNotNull();
        assertThat(parametersList.getParameters())
                .hasSize(5)
                .contains("test1", "test2", "3", "23.45", "a");
    }

    @Test
    void shouldCreateParametersListContainingStringsFromListContainingVariousTypes() {
        // given
        List<Object> testList = List.of("test1", 3, Lists.list(2, 3, 4), "test2");

        // when
        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList(testList);

        // then
        assertThat(parametersList).isNotNull();
        assertThat(parametersList.getParameters())
                .hasSize(4)
                .contains("test1", "test2", "3", "[2, 3, 4]");
    }

    @Test
    void shouldCreateListWithOneStringWhenGivenObjectIsNotList() {
        // given
        Object testObject = "test";

        // when
        YamlParametersList parametersList = new YamlParameterListFactory().createYamlParameterList(testObject);

        // then
        assertThat(parametersList).isNotNull();
        assertThat(parametersList.getParameters())
                .hasSize(1)
                .contains("test");
    }
}
