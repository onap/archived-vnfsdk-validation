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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YamlParameterListFactory {

    public YamlParametersList createEmptyYamlParameterList() {
        return new YamlParametersList(Collections.emptyList());
    }

    public YamlParametersList createYamlParameterList(Object yaml)
        throws YamlParameterListParsingException {
        try {
            return parseYamlToListOfPossibleValues(yaml);
        } catch (ClassCastException e) {
            throw new YamlParameterListParsingException(
                String.format("Fail to parse given objects: %s as list.",yaml), e
            );
        }
    }

    private YamlParametersList parseYamlToListOfPossibleValues(Object yaml) {
        List<String> parametersList = new ArrayList<>();
        if( yaml instanceof List) {
            for (Object element : (List) yaml) {
                parametersList.add(element.toString());
            }
        } else {
            parametersList.add(yaml.toString());
        }
        return new YamlParametersList(parametersList);
    }


    public static class YamlParameterListParsingException extends Exception {

        YamlParameterListParsingException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

}
