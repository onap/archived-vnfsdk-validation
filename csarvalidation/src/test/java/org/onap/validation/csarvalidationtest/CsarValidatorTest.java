/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
package org.onap.validation.csarvalidationtest;

import org.junit.Test;
import org.onap.validation.csar.CsarValidator;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsarValidatorTest {

    String regex = "^\\/[a-zA-Z]\\:\\/";
    ClassLoader classLoader = getClass().getClassLoader();
    Pattern pattern = Pattern.compile(regex);
    private String csarFile = classLoader.getResource("enterprise2DC.csar").getFile();
    Matcher matcher = pattern.matcher(csarFile);
    String dir2 = System.getProperty("file.separator")+csarFile.substring(1);
    String packageId = UUID.randomUUID().toString();

    CsarValidator csarValidator = new CsarValidator(packageId, dir2);

    @Test
    public void testValidateCsarMeta() {
        boolean result = CsarValidator.validateCsarMeta();
        assertEquals(true, result == true);
        System.out.println("inside testValidateCsarMeta : " + result);
    }

    @Test
    public void testValidateCsarIntegrity() {
        boolean result = csarValidator.validateCsarIntegrity(dir2);
        assertEquals(true, result == true);
        System.out.println("inside testValidateCsarIntegrity : " + result);
    }

    @Test
    public void testValidateToscaMeta() {
        boolean result = csarValidator.validateToscaMeta();
        assertEquals(true, result == true);
        System.out.println("inside testValidateToscaMeta : " + result);
    }

}



