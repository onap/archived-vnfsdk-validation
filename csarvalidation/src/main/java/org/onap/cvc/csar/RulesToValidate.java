/*
 * Copyright 2021 Nokia
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
 */
package org.onap.cvc.csar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RulesToValidate {
    private static final String RULES_ATTRIBUTE_SEPARATOR = ",";
    private final String validateRules;

    public RulesToValidate(String rulesToValidate) {
        this.validateRules = rulesToValidate;
    }

    public List<String> get(){
        List<String> rules = new ArrayList<>();

        if(!validateRules.isEmpty()){
            String[] listOfRulesToValidate = validateRules.split(RULES_ATTRIBUTE_SEPARATOR);
            rules.addAll(Arrays.asList(listOfRulesToValidate));
        }

        return rules;
    }
}
