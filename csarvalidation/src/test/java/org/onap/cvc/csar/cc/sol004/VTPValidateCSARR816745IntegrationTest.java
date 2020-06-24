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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;

public class VTPValidateCSARR816745IntegrationTest {

    private static final boolean IS_PNF = true;
    private static final String TEST_CSAR_DIRECTORY = "pnf/r816745/";

    private VTPValidateCSARR816745 testCase;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR816745();
    }

    @Test
    public void shouldNotReportAnyErrorWhenCsarIsNotContainingPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-with-no-pm-dictionary.csar", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionWhenCsarContainsPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-with-pm-dictionary.csar", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // then
        exceptionRule.expect(OnapCommandExecutionFailed.class);
        exceptionRule.expectMessage("Under development");

        // when
        testCase.execute();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R816745");
    }

}
