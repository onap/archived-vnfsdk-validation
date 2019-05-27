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


public class VTPValidateCSARR146092IntegrationTest {

    private VTPValidateCSARR146092 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR146092();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R146092");
    }

    @Test
    public void shouldDoNotReportErrorWhenNonManoArtifactIsNotAvailable() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r146092/missingNonManoArtifactInManifest.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReportThatMandatoryNonManoArtifactSetEntryHasNotAllFields() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r146092/missingFieldsInNonManoArtifactManifest.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [onap_ves_events]",
                "Missing. Entry [onap_pm_dictionary]",
                "Missing. Entry [onap_yang_module]",
                "Missing. Entry [onap_others]"
        );
    }


    @Test
    public void shouldReportThatNonManoArtifactEntryHasAnySource() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r146092/noSourceElementInNonManoArtifactEntryManifest.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [source under onap_ves_events]"
        );
    }


    @Test
    public void shouldReportThatNonManoArtifactEntryHasSourceWithUnknownFile() throws Exception {
        // given
        configureTestCase(testCase, "pnf/r146092/sourceElementWithUnknownFileInNonManoArtifactEntryManifest.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid. Entry [source under onap_ves_events has invalid 'Artifacts/Deployment/Events/RadioNode.yml' path]"
        );
    }

}