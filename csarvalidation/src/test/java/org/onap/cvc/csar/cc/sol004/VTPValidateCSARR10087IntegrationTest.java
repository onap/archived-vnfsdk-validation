/*
 * Copyright 2019 Nokia
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
 *
 */

package org.onap.cvc.csar.cc.sol004;

import org.junit.Before;
import org.junit.Test;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;


public class VTPValidateCSARR10087IntegrationTest {

    private VTPValidateCSARR10087 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR10087();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R10087");
    }

    @Test
    public void shouldReportThatDefinitionYAMLIsNotAvailable() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r10087/noToscaMetaFile.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [Definition YAML]",
                "Ignored. Entry [MainServiceTemplate.mf]",
                "Ignored. Entry [Artifacts]",
                "Ignored. Entry [Definitions]"
        );
    }

    @Test
    public void shouldReportThatDefinitionYAMLDoesNotExist() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r10087/invalidEntryDefinitionsInToscaMeta.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [Entry-Definitions]. Definitions/WRONG_FILE_NAME.yaml does not exist"
        );
    }


    @Test
    public void shouldReportThatManifestFileDoesNotExist() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r10087/invalidManifestFile.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [ETSI-Entry-Manifest]. INVALID_MANIFEST_FILE_NAME.mf does not exist"
        );
    }


    @Test
    public void shouldReportThatChangeHistoryLogFileDoesNotExist() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r10087/invalidChangeHistoryLog.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [ETSI-Entry-Change-Log]. Artifacts/INVALID_FILE_NAME.txt does not exist"
        );
    }

    @Test
    public void shouldReportThatTestDirectoryDoesNotExist() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r10087/invalidTestDirectory.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [Entry-Tests]. Artifacts/InvalidTestsDirectory folder does not exist"
        );
    }




}