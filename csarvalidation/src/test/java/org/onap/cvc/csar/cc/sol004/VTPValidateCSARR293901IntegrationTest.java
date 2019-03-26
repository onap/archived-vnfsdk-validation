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
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;


public class VTPValidateCSARR293901IntegrationTest {

    private VTPValidateCSARR293901 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR293901();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R293901");
    }

    @Test
    public void shouldReportThatMandatoryEntriesAreNotAvailable() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r293901/noMandatoryEntriesInTOSCAMeta.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [Entry-Definitions]",
                "Missing. Entry [ETSI-Entry-Manifest]",
                "Missing. Entry [ETSI-Entry-Change-Log]"
        );
    }

    @Test
    public void shouldDoNotReportAnyErrorWhenAllMandatoryEntriesWereDefined() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r293901/allMandatoryEntriesDefinedInTOSCAMeta.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReportAnyErrorWhneThereIsNoTOSCAMetaFileInTOSCADirectory() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r293901/noTOSCAMetaInTOSCADirectory.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(testCase.getErrors())).contains(
                "Missing. Entry [Definition YAML]",
                "Ignored. Entry [TOSCA-Metadata]",
                "Missing. Entry [ETSI-Entry-Manifest]",
                "Missing. Entry [ETSI-Entry-Change-Log]"
        );
    }
}