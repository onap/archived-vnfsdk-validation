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

package org.onap.cvc.csar;

import org.junit.Test;
import org.onap.cli.main.OnapCli;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;


public class CsarValidatorTest {


    @Test
    public void testAllTestCasesForVNF() throws URISyntaxException {
        OnapCli cli = new OnapCli(new String [] {
                "--product", "onap-dublin",
                "csar-validate",
                "--format", "json",
                "--csar", absoluteFilePath("VoLTE.csar")});
        cli.handle();
        assertEquals(0, cli.getExitCode());
    }


    @Test
    public void testAllTestCasesForPNF_CsarCase() throws URISyntaxException {
        OnapCli cli = new OnapCli(new String [] {
                "--product", "onap-dublin",
                "csar-validate",
                "--format", "json",
                "--pnf",
                "--csar", absoluteFilePath("pnf/r57019/allMandatoryEntriesDefinedInMetadataManifest.csar")});
        cli.handle();
        assertEquals(0, cli.getExitCode());
    }


    @Test
    public void testAllTestCasesForPNF_ZipCase() throws URISyntaxException {
        OnapCli cli = new OnapCli(new String [] {
                "--product", "onap-dublin",
                "csar-validate",
                "--format", "json",
                "--pnf",
                "--csar", absoluteFilePath("pnf/signed-package-valid-signature.zip")});
        cli.handle();
        assertEquals(0, cli.getExitCode());
    }

}
