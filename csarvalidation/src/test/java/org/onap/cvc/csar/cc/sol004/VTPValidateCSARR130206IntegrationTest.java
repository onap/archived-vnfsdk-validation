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
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cvc.csar.CSARArchive;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;


public class VTPValidateCSARR130206IntegrationTest {

    private static final boolean IS_PNF = true;
    public static final String VTP_VALIDATE_SCHEMA = "vtp-validate-csar-r130206.yaml";
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
    public void manual_shouldValidateProperCsarWithCms() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-valid.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isZero();
    }

    @Test
    @Ignore("It is impossible to write test which will always pass, because certificate used to sign the file has time validity." +
        "To verify signed package please please follow instructions from test/resources/README.txt file and comment @Ignore tag. " +
        "Use instructions for option 1. Test was created for manual verification."
    )
    public void manual_shouldValidateCsarWithCertificateInToscaEtsiWithValidSignature() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-tosca-valid.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isZero();
    }

    @Test
    public void shouldReportWarningForMissingCertInCmsToscaMetaAndRootCatalogAndMissingHashCodesInManifest()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-not-secure-warning.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Warning. Consider adding package integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1"
        );
    }

    @Test
    public void shouldReturnNoErrorWhenCertIsOnlyInCmsAndAlgorithmAndHashesAreCorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnNoErrorWhenCertIsOnlyInToscaAndAlgorithmAndHashesAreCorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-tosca.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCsarContainsToscaFileHoweverToscaDoesNotContainsCertEntryAndAlgorithmAndHashesAreCorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-with-tosca-no-cert-entry.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Unable to find ETSI-Entry-Certificate in Tosca file"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertIsOnlyInCmsHoweverHashesAreIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Artifacts/Other/my_script.csh' has wrong hash!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertIsOnlyInToscaHoweverHashesAreIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-tosca-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Artifacts/Deployment/Measurements/PM_Dictionary.yml' has wrong hash!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertIsOnlyInRootDirectoryHoweverHashesAreIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-root-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Artifacts/Deployment/Events/RadioNode_Pnf_v1.yaml' has wrong hash!",
            "Unable to find ETSI-Entry-Certificate in Tosca file",
            "Certificate present in root catalog despite the TOSCA.meta file"
        );
    }

    @Test
    public void shouldReturnErrorWhenToscaEtsiEntryCertificatePointToNotExistingFile()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-with-tosca-cert-pointing-non-existing-cert.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Unable to find cert file defined by ETSI-Entry-Certificate!",
            "Invalid value. Entry [Entry-Certificate]. Artifacts/sample-pnf.cert does not exist"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInTosca()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-tosca.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "File has invalid signature!",
            "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container",
            "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInToscaAndHashIsIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-tosca-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Artifacts/Informational/user_guide.txt' has wrong hash!",
            "File has invalid signature!",
            "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container",
            "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInToscaAndInRootDirectory()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-root-and-tosca.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container",
            "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container",
            "Certificate present in root catalog despite the certificate is included in the signature container",
            "File has invalid signature!"
        );
    }


    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInToscaAndInRootDirectoryAndHashIsIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-root-and-tosca-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(5);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container",
            "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container",
            "Certificate present in root catalog despite the certificate is included in the signature container",
            "Source 'Artifacts/Informational/user_guide.txt' has wrong hash!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInRootDirectory()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-root.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Certificate present in root catalog despite the certificate is included in the signature container",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInCmsAndInRootDirectoryAndHashIsIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-and-root-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Certificate present in root catalog despite the certificate is included in the signature container",
            "Source 'Artifacts/Informational/user_guide.txt' has wrong hash!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInToscaAndInRootDirectory()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-root-and-tosca.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Certificate present in root catalog despite the TOSCA.meta file",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInToscaAndInRootDirectoryAdnHashIsIncorrect()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-root-and-tosca-incorrect-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Certificate present in root catalog despite the TOSCA.meta file",
            "Source 'Artifacts/Deployment/Yang_module/yang-module1.yang' has wrong hash!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnNoErrorWhenCertificateIsLocatedInToscaAndInRootDirectoryHoweverEtsiEntryIsPointingCertificateInRoot()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-root-pointed-by-tosca.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertificateIsLocatedInToscaHoweverManifestDoesNotContainsCms()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-tosca-no-cms.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Unable to find cms signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCsarDoesNotContainsCmsAndCertsHoweverManifestContainsHash()
        throws Exception{
        // given
        configureTestCaseForRule130206("pnf/r130206/csar-no-cms-no-cert-with-hash.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Unable to find cms signature!"
        );
    }

    @Test
    public void shouldReturnNoCertificationErrorWhenCertIsOnlyInRoot() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-root.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        // This test returns other errors that are connected with missing tosca entry,
        // in order to simplify testing, assertion only checks if certificate in root was found and used to validate CMS
        assertThat(convertToMessagesList(errors)).contains(
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnCertificateNotFoundErrorWhenCertIsNotPresentInCmsInRootAndTocsaDirectoryIsMissing() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-no-cert-no-tosca-dir.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        // This test returns other errors that are connected with missing tosca entry,
        // in order to simplify testing, assertion only checks if "certificate not found" error was reported
        assertThat(convertToMessagesList(errors)).contains(
            "Unable to find cert file!"
        );
    }

    @Test
    public void shouldReturnNoErrorWhenCertIsPresentInCmsAndIndividualArtifactHaveCorrectSignature() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-valid-with-signature-of-individual-artifact.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorsWhenCertIsPresentInCmsAndIndividualArtifactHaveOnlySignatureOrCertificate() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-valid-with-only-signature-or-cert-of-individual-artifact.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Files/Scripts/my_script.sh' has certificate tag, but unable to find signature tag!",
            "Source 'Files/pnf-sw-information/pnf-sw-information.yaml' has signature tag, but unable to find certificate tag!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorsWhenCertIsPresentInCmsAndIndividualArtifactHaveSignatureAndCertificateShowingIncorrectFiles() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-valid-with-wrong-path-to-signature-of-individual-artifact.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Files/Yang_module/mynetconf.yang' has signature tag, pointing to non existing file!",
            "Source 'Files/Yang_module/mynetconf.yang' has certificate tag, pointing to non existing file!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenCertIsPresentInCmsAndIndividualArtifactHaveIncorrectSignature() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-cert-in-cms-valid-with-incorrect-signature-of-individual-artifact.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Source 'Files/ChangeLog.txt' has incorrect signature!",
            "File has invalid signature!"
        );
    }

    @Test
    public void shouldReturnErrorWhenOnlyIndividualArtifactHaveSignature() throws Exception {

        // given
        configureTestCaseForRule130206("pnf/r130206/csar-no-cms-with-signature-of-individual-artifact.csar");

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();

        assertThat(convertToMessagesList(errors)).containsExactlyInAnyOrder(
            "Unable to find cms signature!"
        );
    }

    private void configureTestCaseForRule130206(String filePath) throws OnapCommandException, URISyntaxException {
        configureTestCase(testCase, filePath, VTP_VALIDATE_SCHEMA, IS_PNF);
    }
}
