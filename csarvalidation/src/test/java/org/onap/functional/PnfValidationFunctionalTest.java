/*
 * Copyright 2021 Nokia
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
 */

package org.onap.functional;

import org.junit.Test;
import org.onap.functional.cli.OnapCliValidationResponseWrapper;
import org.onap.functional.cli.OnapCliWrapper;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;
import static org.onap.functional.CsarValidationUtility.CERTIFICATION_RULE;
import static org.onap.functional.CsarValidationUtility.MANIFEST_FILE_RULE;
import static org.onap.functional.CsarValidationUtility.NON_MANO_FILES_RULE;
import static org.onap.functional.CsarValidationUtility.OPERATION_STATUS_FAILED;
import static org.onap.functional.CsarValidationUtility.OPERATION_STATUS_PASS;
import static org.onap.functional.CsarValidationUtility.PM_DICTIONARY_YAML_RULE;
import static org.onap.functional.CsarValidationUtility.createExpectedError;
import static org.onap.functional.CsarValidationUtility.getCliCommandValidationResult;
import static org.onap.functional.CsarValidationUtility.verifyThatOperationFinishedWithoutAnyError;


public class PnfValidationFunctionalTest {

    @Test
    public void shouldReportOnlyWarningWhenCsarDoNotHaveCertificateAndHashesInManifest() throws URISyntaxException {
        // given
        OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper expectedWarning =
                createExpectedError(CERTIFICATION_RULE, "0x1006",
                        "Warning. Consider adding package integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1");

        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo("pnf/validFile.csar"));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.errors).isEmpty();
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.warnings)
                        .containsExactlyInAnyOrder(expectedWarning);
            } else {
                assertThat(ruleValidationResult.warnings).isEmpty();
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldNotReportThatPnfValidationFailedWhenZipDoNotHaveCertificatesAndHashesInManifest() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo("pnf/signed-package-valid-signature.zip"));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldPassForCasablancaWithAncestorRuleSet() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfoForSelectedRelease("pnf/signed-package-valid-signature.zip", "casablanca"));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfValidationFailedWhenCsarContainsCertificateInCmsAndInToscaAndInRootAndHashIsIncorrect_allOtherRulesShouldPass() throws URISyntaxException {
        // given
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedErrors =
                List.of(
                        createExpectedError(CERTIFICATION_RULE, "0x4007",
                                "Manifest file has invalid signature!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4004",
                                "Source 'Files/pnf-sw-information/pnf-sw-information.yaml' has wrong hash!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4011",
                                "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4012",
                                "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4013",
                                "Certificate present in root catalog despite the certificate is included in the signature container")
                );
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo("pnf/r130206/cert-in-cms-and-root-and-tosca-incorrect-hash.csar"));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.errors)
                        .containsExactlyInAnyOrderElementsOf(expectedErrors);
            } else {
                assertThat(ruleValidationResult.errors).isEmpty();
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfCertificationRuleValidationFailedWhenCsarContainsCertificateInCmsAndInToscaAndInRootAndHashIsIncorrect() throws URISyntaxException {
        // given
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedErrors =
                List.of(
                        createExpectedError(CERTIFICATION_RULE, "0x4007",
                                "Manifest file has invalid signature!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4004",
                                "Source 'Files/pnf-sw-information/pnf-sw-information.yaml' has wrong hash!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4011",
                                "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4012",
                                "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4013",
                                "Certificate present in root catalog despite the certificate is included in the signature container")
                );
        OnapCliWrapper cli = new OnapCliWrapper(
                createPnfValidationRequestInfoForSelectedRules(
                        "pnf/r130206/cert-in-cms-and-root-and-tosca-incorrect-hash.csar",
                        CERTIFICATION_RULE
                ));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        assertThat(result.results).hasSize(2);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.errors)
                        .containsExactlyInAnyOrderElementsOf(expectedErrors);
            } else {
                assertThat(ruleValidationResult.errors).isEmpty();
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfPmDictionaryYamlRuleValidationSuccessWhenCsarContainsCertificateInCmsAndInToscaAndInRootAndHashIsIncorrect() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(
                createPnfValidationRequestInfoForSelectedRules(
                        "pnf/r130206/cert-in-cms-and-root-and-tosca-incorrect-hash.csar",
                        PM_DICTIONARY_YAML_RULE
                ));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        assertThat(result.results).hasSize(2);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            assertThat(ruleValidationResult.errors).isEmpty();
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfCertificationRuleAndPnfPmDictionaryYamlRuleValidationFailedWhenCsarContainsCertificateInCmsAndInToscaAndInRootAndHashIsIncorrect() throws URISyntaxException {
        // given
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedErrors =
                List.of(
                        createExpectedError(CERTIFICATION_RULE, "0x4007",
                                "Manifest file has invalid signature!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4004",
                                "Source 'Files/pnf-sw-information/pnf-sw-information.yaml' has wrong hash!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4011",
                                "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4012",
                                "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"),
                        createExpectedError(CERTIFICATION_RULE, "0x4013",
                                "Certificate present in root catalog despite the certificate is included in the signature container")
                );
        OnapCliWrapper cli = new OnapCliWrapper(
                createPnfValidationRequestInfoForSelectedRules(
                        "pnf/r130206/cert-in-cms-and-root-and-tosca-incorrect-hash.csar",
                        CERTIFICATION_RULE + "," + PM_DICTIONARY_YAML_RULE
                ));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        assertThat(result.results).hasSize(3);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.errors)
                        .containsExactlyInAnyOrderElementsOf(expectedErrors);
            } else {
                assertThat(ruleValidationResult.errors).isEmpty();
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatIndividualArtifactHaveIncorrectCertificateAndCertificateAndSignatureAreNotPresentAsSources() throws URISyntaxException {
        // given
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedCertificationErrors =
                List.of(
                        createExpectedError(CERTIFICATION_RULE, "0x4020",
                                "Source 'Files/Scripts/my_script.sh' has incorrect signature!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4007",
                                "Manifest file has invalid signature!")
                );
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedManifestErrors =
                List.of(
                        createExpectedError(MANIFEST_FILE_RULE, "0x1001",
                                "file(s): [Files/Scripts/my_script.cert, Files/Scripts/my_script.sig.cms] available in CSAR, but cannot be found in Manifest as Source",
                                "TOSCA-Metadata"
                        )
                );
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(
                "pnf/r130206/csar-cert-in-cms-valid-with-incorrect-signature-of-individual-artifact.csar"
        ));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.errors)
                        .containsExactlyInAnyOrderElementsOf(expectedCertificationErrors);
            } else if (ruleValidationResult.vnfreqName.equals(MANIFEST_FILE_RULE)) {
                assertThat(ruleValidationResult.errors)
                        .containsExactlyInAnyOrderElementsOf(expectedManifestErrors);
            } else {
                assertThat(ruleValidationResult.errors).isEmpty();
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatIndividualArtifactsHaveMultipleIncorrectCertificatesAndSignatures() throws URISyntaxException {
        // given
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedCertificationErrors =
                List.of(
                        createExpectedError(CERTIFICATION_RULE, "0x4004",
                "Source 'Artifacts/Deployment/Events/RadioNode_Pnf_v1.yaml' has wrong hash!"),
                createExpectedError(CERTIFICATION_RULE, "0x4020",
                    "Source 'Artifacts/Deployment/Events/RadioNode_Pnf_v2.yaml' has incorrect signature!"),
                createExpectedError(CERTIFICATION_RULE, "0x4018",
                                "Source 'Artifacts/Deployment/Measurements/PM_Dictionary.yml' has 'signature' tag, pointing to non existing file!. Pointed file 'Artifacts/Deployment/Measurements/PM_Dictionary.sig.cms'"),
                        createExpectedError(CERTIFICATION_RULE, "0x4023",
                                "Source 'Artifacts/Deployment/Yang_module/yang-module1.yang' has 'signature' file with wrong name, signature name: 'yang-module.sig.cms'.Signature should have same name as source file!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4023",
                                "Source 'Artifacts/Deployment/Yang_module/yang-module1.yang' has 'certificate' file with wrong name, signature name: 'yang-module.cert'.Signature should have same name as source file!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4020",
                                "Source 'Artifacts/Other/my_script.csh' has incorrect signature!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4022",
                                "Source 'Artifacts/Informational/user_guide.txt' has 'signature' file located in wrong directory, directory: 'Artifacts/user_guide.sig.cms'.Signature should be in same directory as source file!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4022",
                                "Source 'Artifacts/Informational/user_guide.txt' has 'certificate' file located in wrong directory, directory: 'Artifacts/user_guide.cert'.Signature should be in same directory as source file!"),
                        createExpectedError(CERTIFICATION_RULE, "0x4007",
                                "Manifest file has invalid signature!")
                );
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedManifestErrors =
                List.of(
                        createExpectedError(MANIFEST_FILE_RULE, "0x1001",
                                "file(s): [TOSCA-Metadata/TOSCA.meta, Definitions/MainServiceTemplate.yaml, Artifacts/Deployment/Yang_module/yang-module2.yang, Artifacts/Deployment/Yang_module/yang-module.cert, Artifacts/Deployment/Yang_module/yang-module.sig.cms, Artifacts/ChangeLog.txt, Artifacts/sample-pnf.cert] available in CSAR, but cannot be found in Manifest as Source",
                                "TOSCA-Metadata"
                        )
                );
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedPnfDictionaryErrors =
                List.of(
                        createExpectedError(PM_DICTIONARY_YAML_RULE, "0x2000",
                                "Fail to load PM_Dictionary With error: Dictionary YAML file is empty",
                                "Artifacts/Deployment/Measurements/PM_Dictionary.yml"
                        )
                );
        List<OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedNonManoFilesErrors =
                List.of(
                        createExpectedError(NON_MANO_FILES_RULE, "0x2002",
                                "Missing. Entry [Source under onap_ves_events]",
                                "MainServiceTemplate.mf"
                        ),
                        createExpectedError(NON_MANO_FILES_RULE, "0x2002",
                                "Missing. Entry [onap_yang_module]",
                                "MainServiceTemplate.mf"
                        ),
                        createExpectedError(NON_MANO_FILES_RULE, "0x2002",
                                "Missing. Entry [Source under onap_others]]",
                                "MainServiceTemplate.mf"
                        ),
                        createExpectedError(NON_MANO_FILES_RULE, "0x2002",
                                "Missing. Entry [Source under onap_pm_dictionary]",
                                "MainServiceTemplate.mf"
                        )
                );
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(
                "pnf/r130206/csar-cert-in-tosca-multiple-individual-signature.csar"
        ));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        result.results.forEach((ruleValidationResult) -> {
            assertThat(ruleValidationResult.warnings).isEmpty();
            switch (ruleValidationResult.vnfreqName) {
                case CERTIFICATION_RULE:
                    assertThat(ruleValidationResult.errors)
                            .containsExactlyInAnyOrderElementsOf(expectedCertificationErrors);
                    break;
                case MANIFEST_FILE_RULE:
                    assertThat(ruleValidationResult.errors)
                            .containsExactlyInAnyOrderElementsOf(expectedManifestErrors);
                    break;
                case PM_DICTIONARY_YAML_RULE:
                    assertThat(ruleValidationResult.errors)
                            .containsExactlyInAnyOrderElementsOf(expectedPnfDictionaryErrors);
                    break;
                case NON_MANO_FILES_RULE:
                    assertThat(ruleValidationResult.errors)
                            .containsExactlyInAnyOrderElementsOf(expectedNonManoFilesErrors);
                    break;
                default:
                    assertThat(ruleValidationResult.errors).isEmpty();
                    break;
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    private String[] createPnfValidationRequestInfo(String csarPath) throws URISyntaxException {
        return new String[]{
                "--product", "onap-vtp",
                "csar-validate",
                "--format", "json",
                "--pnf",
                "--csar", absoluteFilePath(csarPath)
        };
    }

    private String[] createPnfValidationRequestInfoForSelectedRelease(String csarPath, String release) throws URISyntaxException {
        return new String[]{
                "--product", "onap-vtp",
                "csar-validate",
                "--release", release,
                "--format", "json",
                "--pnf",
                "--csar", absoluteFilePath(csarPath)
        };
    }

    private String[] createPnfValidationRequestInfoForSelectedRules(String csarPath, String rulesToValidate) throws URISyntaxException {
        return new String[]{
                "--product", "onap-vtp",
                "csar-validate",
                "--format", "json",
                "--rules", rulesToValidate,
                "--pnf",
                "--csar", absoluteFilePath(csarPath)
        };
    }
}
