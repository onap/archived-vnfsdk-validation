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
import org.onap.validation.csar.FileUtil;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsarValidatorTest {

    String regex = "^\\/[a-zA-Z]\\:\\/";
    ClassLoader classLoader = getClass().getClassLoader();
    Pattern pattern = Pattern.compile(regex);
    private String csarFile = classLoader.getResource("enterprise2DC.csar").getFile();
    Matcher matcher = pattern.matcher(csarFile);
    String sample1 = System.getProperty("file.separator") + csarFile.substring(1);
    String packageId = UUID.randomUUID().toString();

    private String csarFile2 = classLoader.getResource("sample2.csar").getFile();
    String sample2 = System.getProperty("file.separator") + csarFile2.substring(1);
    String packageId2 = UUID.randomUUID().toString();
/*
    private String csarFile3 = classLoader.getResource("sample4.csar").getFile();
    String sample3 = System.getProperty("file.separator") + csarFile3.substring(1);
    String packageId3 = UUID.randomUUID().toString(); */
    @Test
    public void testAll() {
        CsarValidator csarValidator = new CsarValidator(packageId, sample1);
        testValidateCsar(csarValidator);

        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
      //  CsarValidator csarValidator3 = new CsarValidator(packageId3, sample3);
        testValidateCsar(csarValidator2);
        String sample1Dir = sample1.replace(".csar", "");
		String sample2Dir = sample2.replace(".csar", "");

		boolean result = FileUtil.deleteDirectory(sample1Dir);
		boolean result1 = FileUtil.deleteDirectory(sample2Dir);

		assertEquals(true, result == true && result1 ==true);
    }

    @Test
    public void testIndividual() {
        CsarValidator csarValidator = new CsarValidator(packageId, sample1);
        testValidateCsarMeta(csarValidator);
        testValidateCsarIntegrity(csarValidator);
        testValidateToscaMeta(csarValidator);

        CsarValidator csarValidator2 = new CsarValidator(packageId2, sample2);
        testValidateCsarIntegrity(csarValidator2);
        testValidateToscaMeta(csarValidator2);
        testValidateMainService(csarValidator2);
        //Rel1 specific test case
        String dir3 = sample1.replace(".csar", "");
		String dir4 = sample2.replace(".csar", "");
		boolean result = FileUtil.deleteDirectory(dir3);
		boolean result1 = FileUtil.deleteDirectory(dir4);
		assertEquals(true, result == true && result1 ==true);
    }
    @Test
	public void testCloseInputStream() {
		InputStream dir = null;
		 FileUtil.closeInputStream(dir);
		 assertTrue(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadJsonDatafFromFile() {
		FileUtil.readJsonDatafFromFile(sample1, null);
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
		FileUtil.closeFileStream(dir3 );
	}
	@Test
	public void testCloseOutptutStream() {
		OutputStream dir4 = new OutputStream() {

			@Override
			public void write(int b) throws IOException {

			}
		};
		FileUtil.closeOutputStream(dir4);
	}

   /* @Test
	public void testDeleteDirectory() throws IOException {
    	String dir4 = dir2.replace(".csar", "");
		String dir3 = sample2.replace(".csar", "");
		boolean result = FileUtil.deleteDirectory(dir3);
		boolean result1 = FileUtil.deleteDirectory(dir3);
		assertEquals(true, result == true && result1 ==true);
	}*/

    private void testValidateCsarMeta(CsarValidator cv) {
        boolean result = cv.validateCsarMeta();
        assertEquals(true, result == true);
    }


    private void testValidateCsarIntegrity(CsarValidator cv) {
        boolean result = cv.validateCsarIntegrity(sample1);
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
