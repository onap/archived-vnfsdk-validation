/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.validation.csarvalidationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.onap.validation.csar.CommonConstants;
import org.onap.validation.csar.CsarValidator;
import org.onap.validation.csar.FileUtil;

public class CsarValidatorTest {

    String regex = "^\\/[a-zA-Z]\\:\\/";

    ClassLoader classLoader = getClass().getClassLoader();

    Pattern pattern = Pattern.compile(regex);

    private String csarFile = classLoader.getResource("enterprise2DC.csar").getFile();

    Matcher matcher = pattern.matcher(csarFile);

    String sample1 = System.getProperty("file.separator") + csarFile.substring(1);

    String packageId = UUID.randomUUID().toString();

    private String csarFile2 = classLoader.getResource("vEPC_NS.csar").getFile();

    String sample2 = System.getProperty("file.separator") + csarFile2.substring(1);

    String packageId2 = UUID.randomUUID().toString();

    private String csarFile3 = classLoader.getResource("vIMS_NS.csar").getFile();

    String sample3 = System.getProperty("file.separator") + csarFile3.substring(1);

    String packageId3 = UUID.randomUUID().toString();

    private String csarFile4 = classLoader.getResource("VoLTE.csar").getFile();

    String sample4 = System.getProperty("file.separator") + csarFile4.substring(1);

    String packageId4 = UUID.randomUUID().toString();

    private String csarFile5 = classLoader.getResource("sample2.csar").getFile();

    String sample5 = System.getProperty("file.separator") + csarFile5.substring(1);

    String packageId5 = UUID.randomUUID().toString();

    @Test
    public void testAll() throws IOException, InterruptedException {
        CsarValidator csarValidator = new CsarValidator(packageId, sample1);
        testValidateCsar(csarValidator);
        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
        testValidateCsar(csarValidator2);
        CsarValidator csarValidator3 = new CsarValidator(packageId3, sample3);
        testValidateCsar(csarValidator3);
        CsarValidator csarValidator4 = new CsarValidator(packageId4, sample4);
        testValidateCsar(csarValidator4);
        CsarValidator csarValidator5 = new CsarValidator(packageId5, sample5);
        testValidateCsar(csarValidator5);
        String sample1Dir = sample1.replace(".csar", "");
        String sample2Dir = sample2.replace(".csar", "");
        String sample3Dir = sample3.replace(".csar", "");
        String sample4Dir = sample4.replace(".csar", "");
        String sample5Dir = sample5.replace(".csar", "");
        boolean result = FileUtil.deleteDirectory(sample1Dir);
        boolean result1 = FileUtil.deleteDirectory(sample2Dir);
        boolean result2 = FileUtil.deleteDirectory(sample3Dir);
        boolean result3 = FileUtil.deleteDirectory(sample4Dir);
        boolean result4 = FileUtil.deleteDirectory(sample5Dir);
        assertEquals(true, result == true && result1 == true && result2 == true && result3 == true && result4 == true);
    }

    @Test
    public void testIndividual() throws IOException, InterruptedException {
        CsarValidator csarValidator = new CsarValidator(packageId, sample1);
        testValidateCsarMeta(csarValidator);
        testValidateCsarIntegrity(csarValidator);
        testValidateToscaMeta(csarValidator);

        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
        testValidateCsarIntegrity(csarValidator2);
        testValidateToscaMeta(csarValidator2);
        testValidateMainService(csarValidator2);

        CsarValidator csarValidator3 = new CsarValidator(packageId3, sample3);
        testValidateCsarIntegrity(csarValidator3);
        testValidateToscaMeta(csarValidator3);
        testValidateMainService(csarValidator3);

        CsarValidator csarValidator4 = new CsarValidator(packageId4, sample4);
        testValidateCsarIntegrity(csarValidator4);
        testValidateToscaMeta(csarValidator4);
        testValidateMainService(csarValidator4);

        CsarValidator csarValidator5 = new CsarValidator(packageId5, sample5);
        testValidateCsarIntegrity(csarValidator5);
        testValidateToscaMeta(csarValidator5);
        testValidateMainService(csarValidator5);
        // Rel1 specific test case
        String sample1Dir = sample1.replace(".csar", "");
        String sample2Dir = sample2.replace(".csar", "");
        String sample3Dir = sample3.replace(".csar", "");
        String sample4Dir = sample4.replace(".csar", "");
        String sample5Dir = sample5.replace(".csar", "");
        boolean result = FileUtil.deleteDirectory(sample1Dir);
        boolean result1 = FileUtil.deleteDirectory(sample2Dir);
        boolean result2 = FileUtil.deleteDirectory(sample3Dir);
        boolean result3 = FileUtil.deleteDirectory(sample4Dir);
        boolean result4 = FileUtil.deleteDirectory(sample5Dir);
        assertEquals(true, result == true && result1 == true && result2 == true && result3 == true && result4 == true);
    }

    @Test
    public void testCloseInputStream() {
        InputStream dir = null;
        FileUtil.closeInputStream(dir);
        assertTrue(true);
    }

    @Test
    public void testCloseZipFile() throws ZipException, IOException {
        File file = new File(sample1);
        ZipFile dir1 = new ZipFile(file);
        FileUtil.closeZipFile(dir1);
        assertTrue(true);
    }

    @Test
    public void testCloseFileStream() throws FileNotFoundException {
        FileInputStream dir3 = new FileInputStream(sample1);
        FileUtil.closeFileStream(dir3);
    }

    @Test
    public void testCloseOutptutStream() {
        OutputStream dir4 = new OutputStream() {

            @Override
            public void write(int number) throws IOException {

            }
        };
        FileUtil.closeOutputStream(dir4);
    }

    private void testValidateCsarMeta(CsarValidator cv) {
        String result = CsarValidator.validateCsarMeta();
        assertEquals(true, result == CommonConstants.SUCCESS_STR);
    }

    private void testValidateCsarIntegrity(CsarValidator cv) {
        boolean result = CsarValidator.validateCsarIntegrity(sample1);
        assertEquals(true, result == true);
    }

    private void testValidateToscaMeta(CsarValidator cv) {
        String result = CsarValidator.validateAndScanToscaMeta();
        assertEquals(true, result == CommonConstants.SUCCESS_STR);
    }

    private void testValidateMainService(CsarValidator cv) {

        String result = CsarValidator.validateMainService();
        assertEquals(true, result == CommonConstants.SUCCESS_STR);
    }

    private void testValidateCsar(CsarValidator cv) {

        String result = CsarValidator.validateCsar();
        assertEquals(true, result == CommonConstants.SUCCESS_STR);
    }
}
