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


public class VTPValidateCSARR787965IntegrationTest {

    private VTPValidateCSARR787965 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR787965();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R787965");
    }

    @Test
    public void shouldReportThatCsarHasInvalidSignature() throws Exception {

        // given
        configureTestCase(testCase, "pnf/signed-package-invalid-signature.zip");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid CSAR signature!"
        );
    }

    @Test
    public void shouldReportThatZipContainsSignatureWithCertificationFileAndPackageIsProbableValid() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r787965/signature-and-certificate.zip");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Warning. Zip package probably is valid. " +
                        "It contains only signature with certification cms and csar package. " +
                        "Unable to verify csar signature."
        );
    }

    @Test
    public void shouldReportThatZipPackageIsBroken() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r787965/broken.zip");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Unable to find certification files."
        );
    }

    @Test
    public void shouldDoNotReportAnyErrorWhenPackageHasValidSignature() throws Exception {

        // given
        configureTestCase(testCase, "pnf/signed-package-valid-signature.zip");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

}