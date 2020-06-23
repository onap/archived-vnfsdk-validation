/*
 * Copyright 2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

public class VTPValidateCSARR01123IntegrationTest {

    private static final boolean IS_PNF = true;
    private static final String TEST_CSAR_DIRECTORY = "pnf/r01123/";

    private VTPValidateCSARR01123 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR01123();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R01123");
    }

    @Test
    public void shouldReportThatFileIsNotPresentInSources() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-option1-invalid.csar", "vtp-validate-csar-r01123.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
            "files: TOSCA-Metadata/TOSCA.meta, Artifacts/Deployment/Yang_module/yang-module2.yang, Artifacts/Deployment/Yang_module/yang-module1.yang, Artifacts/Informational/user_guide.txt, Artifacts/sample-pnf.cert are located in CSAR, but are not located in Manifest as Source"
        );
    }

    @Test
    public void shouldNotReportAnyErrorWhenAllFilesPresentInCsarArePresentInSources() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-option1-valid.csar", "vtp-validate-csar-r01123.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

}
