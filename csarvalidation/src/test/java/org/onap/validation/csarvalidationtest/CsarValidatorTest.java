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
    String dir2 = System.getProperty("file.separator") + csarFile.substring(1);
    String packageId = UUID.randomUUID().toString();

    private String csarFile2 = classLoader.getResource("sample2.csar").getFile();
    String sample2 = System.getProperty("file.separator") + csarFile2.substring(1);
    String packageId2 = UUID.randomUUID().toString();


    @Test
    public void testAll() {
        CsarValidator csarValidator = new CsarValidator(packageId, dir2);
        testValidateCsar(csarValidator);

        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
        testValidateCsar(csarValidator2);
    }

    @Test
    public void testIndividual() {
        CsarValidator csarValidator = new CsarValidator(packageId, dir2);
        testValidateCsarMeta(csarValidator);
        testValidateCsarIntegrity(csarValidator);
        testValidateToscaMeta(csarValidator);

        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
        testValidateCsarIntegrity(csarValidator2);
        testValidateToscaMeta(csarValidator2);
        testValidateMainService(csarValidator2);  //Rel1 specific test case
    }



    private void testValidateCsarMeta(CsarValidator cv) {
        boolean result = cv.validateCsarMeta();
        assertEquals(true, result == true);
    }


    private void testValidateCsarIntegrity(CsarValidator cv) {
        boolean result = cv.validateCsarIntegrity(dir2);
        assertEquals(true, result == true);
        }


    private void testValidateToscaMeta(CsarValidator cv) {
        boolean result = cv.validateToscaMeta();
        assertEquals(true, result == true);
    }

    private void testValidateMainService(CsarValidator cv) {

        boolean result = cv.validateMainService();
        assertEquals(true, result == true);
    }

    private void testValidateCsar(CsarValidator cv) {

        boolean result = cv.validateCsar();
        assertEquals(true, result == true);
    }
}
