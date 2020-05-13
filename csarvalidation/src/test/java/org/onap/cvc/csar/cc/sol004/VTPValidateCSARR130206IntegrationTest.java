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
import org.junit.Ignore;
import org.junit.Test;
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;


public class VTPValidateCSARR130206IntegrationTest {

    private static final boolean IS_PNF = true;
    private VTPValidateCSARR130206 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR130206();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R130206");
    }

    @Test
    @Ignore("It is impossible to write test which will always pass, because certificate used to sign the file has time validity." +
            "To verify signed package please please follow instructions from test/resources/README.txt file and comment @Ignore tag. " +
            "Use instructions for option 1. Test was created for manual verification."
    )
    public void manual_shouldValidateProperCsar() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r130206/csar-option1-valid.csar", "vtp-validate-csar-r130206.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    @Ignore("It is impossible to write test which will always pass, because certificate used to sign the file has time validity." +
            "To verify signed package please please follow instructions from test/resources/README.txt file and comment @Ignore tag. " +
            "Use instructions for option 1. Test was created for manual verification."
    )
    public void manual_shouldValidateCsarWithCertificateInEtsiAndMissingInCMS() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r130206/csar-with-etsi-cert-without-cert-in-cms.csar", "vtp-validate-csar-r130206.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldReportThatOnlySignatureIsInvalid() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r130206/csar-option1-validSection.csar", "vtp-validate-csar-r130206.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(convertToMessagesList(errors)).contains(
                "File has invalid CMS signature!",
                "Mismatch between contents of non-mano-artifact-sets and source files of the package"
        );
    }

    @Test
    public void shouldReportErrorsForInvalidCsar() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r130206/csar-option1-invalid.csar", "vtp-validate-csar-r130206.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(6);
        assertThat(convertToMessagesList(errors)).contains(
                "Unable to find CMS section in manifest!",
                "Source 'Definitions/MainServiceTemplate.yaml' has wrong hash!",
                "Source 'Artifacts/Other/my_script.csh' has hash, but unable to find algorithm tag!",
                "Unable to calculate digest - file missing: Artifacts/NonExisting2.txt",
                "Mismatch between contents of non-mano-artifact-sets and source files of the package",
                "File has invalid CMS signature!"
        );
    }


    @Test
    public void shouldReportThanInVnfPackageCertFileWasNotDefined() throws Exception {

        // given
        configureTestCase(testCase, "sample2.csar", "vtp-validate-csar-r130206.yaml", false);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(convertToMessagesList(errors)).contains(
                "Unable to find cert file defined by Entry-Certificate!",
                "Unable to find CMS section in manifest!",
                "Missing. Entry [tosca_definitions_version]"
        );
    }


    @Test
    public void shouldReportThanInVnfPackageETSIFileIsMissingAndNoCertificateInCMS() throws Exception {

        // given
        configureTestCase(testCase, "pnf/r130206/csar-with-no-certificate.csar", "vtp-validate-csar-r130206.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(convertToMessagesList(errors)).contains(
                "Unable to find cert file defined by ETSI-Entry-Certificate!",
                "Unable to find CMS section in manifest!"

        );
    }


}
