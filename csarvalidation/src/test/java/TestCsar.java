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
package org.onap.validation.csar;
import org.junit.Test;
import java.io.File;
public class TestCsar{
    CsarParser csarParser = new CsarParser();

    String filePath = "D:/NewFolder/Sep14/validation/csarvalidation/src/test/resources/enterprise2DC.csar";
	/*
    try {
        String current = new java.io.File(".").getCanonicalPath();
        System.out.println("Current dir:"+current);
    } catch(IOException e){}
*/
    @Test
    public void testCsarExtract() {

        System.out.println("In Test case:");
        System.out.println("filePath: "+filePath);
        @SuppressWarnings("static-access")
        boolean result = csarParser.csarExtract(filePath);
        System.out.println("output of CsarParser.csarExtract method is : "+result);
    }

    @Test
    public void testValidateCsarMeta() {
        boolean result = CsarParser.validateCsarMeta();
        System.out.println("output of CsarParser.validateCsarMeta method is : "+result);
    }

}