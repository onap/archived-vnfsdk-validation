/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
 * Copyright 2020 Nokia
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

package org.onap.cvc.csar;

import com.google.gson.Gson;
import org.junit.Test;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cli.main.OnapCli;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;


public class CsarValidatorTest {

    private static final String CERTIFICATION_RULE = "r130206";
    private static final String OPERATION_STATUS_FAILED = "FAILED";
    private static final String OPERATION_STATUS_PASS = "PASS";

    @Test
    public void shouldReportThanVnfValidationFailed() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createExecutionRequestData(false, "VoLTE.csar"));

        // when
        cli.handle();

        // then
        final CliValidationResponseWrapper result = getCliCommandValidationResult(cli);
        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        result.results.forEach((ruleValidationResult)->{
            if ( ruleHaveOneOfCodes(ruleValidationResult.vnfreqName,
                "r01123", "r09467")
            ) {
                assertThat(ruleValidationResult.errors)
                    .hasSize(2);
            } else if ( ruleHaveOneOfCodes(ruleValidationResult.vnfreqName,
                "r21322","r26885","r43958" ,"r66070","r130206")
            ) {
                assertThat(ruleValidationResult.errors)
                    .hasSize(1);
            } else {
                assertThat(ruleValidationResult.errors).hasSize(0);
            }
        });

        verifyThatOperationFinishedWithoutAnyError(cli);
    }


    @Test
    public void shouldReportOnlyWarningWhenCsarDoNotHaveCertificateAndHashesInManifest() throws URISyntaxException {
        // given
        CliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper expectedWarning =
            createExpectedError(CERTIFICATION_RULE, "0x1006",
                "Warning. Consider adding package integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1");

        OnapCliWrapper cli = new OnapCliWrapper(createExecutionRequestData(true, "pnf/validFile.csar"));

        // when
        cli.handle();

        // then
        final CliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        result.results.forEach((ruleValidationResult)->{
            assertThat(ruleValidationResult.errors).hasSize(0);
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.warnings)
                    .hasSize(1)
                    .containsOnly(expectedWarning);
            } else {
                assertThat(ruleValidationResult.warnings).hasSize(0);
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    private CliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper createExpectedError(
        String rule, String errorCode, String errorMessage
    ) {
        return new CliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper(
            rule.toUpperCase(), errorCode,
            errorMessage,
            "", "-1"
        );
    }

    @Test
    public void shouldNotReportThatPnfValidationFailedWhenZipDoNotHaveCertificatesAndHashesInManifest() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createExecutionRequestData(true, "pnf/signed-package-valid-signature.zip"));

        // when
        cli.handle();

        // then
        final CliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_PASS);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfValidationFailedWhenCsarContainsCertificateInCmsAndInToscaAndInRootAndHashIsIncorrect_allOtherRulesShouldPass() throws URISyntaxException {
        // given

        List<CliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper> expectedErrors =
            List.of(
                createExpectedError(CERTIFICATION_RULE, "0x4007",
                    "File has invalid signature!"),
                createExpectedError(CERTIFICATION_RULE, "0x4004",
                    "Source 'Files/pnf-sw-information/pnf-sw-information.yaml' has wrong hash!"),
                createExpectedError(CERTIFICATION_RULE, "0x4011",
                    "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container"),
                createExpectedError(CERTIFICATION_RULE, "0x4012",
                    "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container"),
                createExpectedError(CERTIFICATION_RULE, "0x4013",
                    "Certificate present in root catalog despite the certificate is included in the signature container")
            );
        OnapCliWrapper cli = new OnapCliWrapper(createExecutionRequestData(true, "pnf/r130206/cert-in-cms-and-root-and-tosca-incorrect-hash.csar"));

        // when
        cli.handle();

        // then
        final CliValidationResponseWrapper result = getCliCommandValidationResult(cli);

        assertThat(result.criteria).isEqualTo(OPERATION_STATUS_FAILED);
        result.results.forEach((ruleValidationResult)->{
            assertThat(ruleValidationResult.warnings).hasSize(0);
            if (ruleValidationResult.vnfreqName.equals(CERTIFICATION_RULE)) {
                assertThat(ruleValidationResult.errors)
                    .hasSize(5)
                    .containsAll(expectedErrors);
            } else {
                assertThat(ruleValidationResult.errors).hasSize(0);
            }
        });
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    private String[] createExecutionRequestData(boolean isPnf, String csarPath) throws URISyntaxException {
        String[] executionParameters;
        if (isPnf) {
            executionParameters = new String[]{
                "--product", "onap-dublin",
                "csar-validate",
                "--format", "json",
                "--pnf",
                "--csar", absoluteFilePath(csarPath)
            };
        } else {
            executionParameters = new String[]{
                "--product", "onap-dublin",
                "csar-validate",
                "--format", "json",
                "--csar", absoluteFilePath(csarPath)
            };
        }
        return executionParameters;
    }

    private CliValidationResponseWrapper getCliCommandValidationResult(OnapCliWrapper cli) {
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        return new Gson().fromJson(onapCommandResult.getOutput().toString(), CliValidationResponseWrapper.class);
    }

    private boolean ruleHaveOneOfCodes(String ruleCode, String... codes ) {
        return Arrays.asList(codes).contains(ruleCode);
    }

    private void verifyThatOperationFinishedWithoutAnyError(OnapCliWrapper cli) {
        assertThat(cli.getExitCode()).isEqualTo(0);
    }

    static class OnapCliWrapper extends OnapCli {

        private OnapCommandResult commandResult;

        OnapCliWrapper(String[] args) {
            super(args);
        }

        @Override
        public void handleTracking(OnapCommand cmd) throws OnapCommandException {
            super.handleTracking(cmd);
            this.commandResult = cmd.getResult();
        }

        OnapCommandResult getCommandResult() {
            return this.commandResult;
        }
    }


    static class CliValidationResponseWrapper {

        VnfDataWrapper vnf;
        String data;
        String criteria;
        List<ValidationResultWrapper> results;
        String contact;
        String platform;

        static class VnfDataWrapper {
            String name;
            String vendor;
            String version;
            String type;
            String mode;
        }

        static class ValidationResultWrapper {
            Boolean passed;
            String vnfreqName;
            String description;
            List<ValidationErrorWrapper> errors;
            List<ValidationErrorWrapper> warnings;

            static class ValidationErrorWrapper {
                String vnfreqNo;
                String code;
                String message;
                String file;
                String lineNumber;

                public ValidationErrorWrapper(String vnfreqNo, String code, String message, String file, String lineNumber) {
                    this.vnfreqNo = vnfreqNo;
                    this.code = code;
                    this.message = message;
                    this.file = file;
                    this.lineNumber = lineNumber;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    return o.hashCode() == this.hashCode();
                }

                @Override
                public int hashCode() {
                    return Objects.hash(vnfreqNo, code, message, file, lineNumber);
                }
            }

        }

    }
}
