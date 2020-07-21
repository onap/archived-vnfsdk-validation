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

import org.assertj.core.api.Condition;
import org.assertj.core.api.HamcrestCondition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToFilesList;

public class VTPValidateCSARR816745IntegrationTest {

    private static final boolean IS_PNF = true;
    private static final String TEST_CSAR_DIRECTORY = "pnf/r816745/";
    private static final int NUMBER_OF_EXPECTED_ERRORS = 4;

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
    public void shouldReturnNoErrorsWhenCsarContainsValidPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-with-valid-pm-dictionary.csar", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnNoErrorsWhenZipContainsCsarWithValidPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "zip-with-valid-pm-dictionary.zip", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnListOfErrorsWhenCsarContainsInvalidPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-with-invalid-pm-dictionary.csar", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        assertThatReturnedErrorsAreCorrect(testCase.getErrors());
    }

    @Test
    public void shouldReturnListOfErrorsWhenZipContainsCsarWithInvalidPmDictionary() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "zip-with-invalid-pm-dictionary.zip", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        assertThatReturnedErrorsAreCorrect(testCase.getErrors());
    }

    @Test
    public void shouldAddPmDictionaryLoadingErrorWhenGivenInvalidPath() throws Exception {
        // given
        configureTestCase(testCase, TEST_CSAR_DIRECTORY + "csar-with-empty-pm-dictionary.csar", "vtp-validate-csar-r816745.yaml", IS_PNF);

        // when then
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains("Fail to load PM_Dictionary With error: PM_Dictionary YAML file is empty");
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R816745");
    }

    private void assertThatReturnedErrorsAreCorrect(List<CSARArchive.CSARError> errors) {
        assertThat(errors.size()).isEqualTo(NUMBER_OF_EXPECTED_ERRORS);

        Condition<String> containingSameFileForAllErrors = new HamcrestCondition<>(
            containsString("Artifacts/Deployment/Measurements/PM_Dictionary.yml")
        );
        assertThat(convertToFilesList(errors))
            .haveExactly(NUMBER_OF_EXPECTED_ERRORS, containingSameFileForAllErrors);

        Condition<String> containingErrorForMissingValueInFirstDocument = new HamcrestCondition<>(allOf(
            containsString("Invalid YAML document in PM_Dictionary file."),
            containsString("In document number 1"),
            containsString("Path: /pmMetaData/pmFields/"),
            containsString("Key not found: measChangeType")
        ));
        assertThat(convertToMessagesList(errors)).haveExactly(1, containingErrorForMissingValueInFirstDocument);

        Condition<String> containingErrorForWrongValueInFirstDocument = new HamcrestCondition<>(allOf(
            containsString("Invalid YAML document in PM_Dictionary file."),
            containsString("In document number 1"),
            containsString("Path: /pmMetaData/pmFields/measResultType"),
            containsString("Value(s) is/are not in array of accepted values."),
            containsString("value(s):  integer"),
            containsString("accepted value(s):  [float, uint32, uint64]")
        ));
        assertThat(convertToMessagesList(errors)).haveExactly(1, containingErrorForWrongValueInFirstDocument);

        Condition<String> containingErrorForMissingValueInSecondDocument = new HamcrestCondition<>(allOf(
            containsString("Invalid YAML document in PM_Dictionary file."),
            containsString("In document number 2"),
            containsString("Path: /pmMetaData/pmFields/"),
            containsString("Key not found: measChangeType")
        ));
        assertThat(convertToMessagesList(errors)).haveExactly(1, containingErrorForMissingValueInSecondDocument);

        Condition<String> containingErrorForWrongValueInArrayInThirdDocument = new HamcrestCondition<>(allOf(
            containsString("Invalid YAML document in PM_Dictionary file."),
            containsString("In document number 3"),
            containsString("Path: /pmMetaData/pmFields/measAdditionalFields/vendorField1"),
            containsString("Value(s) is/are not in array of accepted values."),
            containsString("value(s):  [Z, A]"),
            containsString("accepted value(s):  [X, Y, Z]")
        ));
        assertThat(convertToMessagesList(errors)).haveExactly(1, containingErrorForWrongValueInArrayInThirdDocument);
    }

}
