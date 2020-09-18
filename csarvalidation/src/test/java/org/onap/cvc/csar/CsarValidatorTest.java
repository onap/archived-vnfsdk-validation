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

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cli.fw.output.OnapCommandResultAttribute;
import org.onap.cli.main.OnapCli;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;


public class CsarValidatorTest {

    private static final String NO_CERTIFICATE_RULE = "r130206";
    private static final String OPERATION_STATUS_FAILED = "FAILED";

    @Test
    public void shouldReportErrorAsWarningWhenErrorIsIgnored() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(new String[]{
            "--product", "onap-dublin",
            "csar-validate",
            "--format", "json",
            "--pnf",
            "--csar", absoluteFilePath("pnf/r130206/csar-option1-warning-2.csar")});

        // when
        cli.handle();

        // then
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        assertTrue(onapCommandResult.getOutput().toString().contains(
            "\"warnings\":[{\"vnfreqNo\":\"R130206\",\"code\":\"0x1006\",\"message\":\"Warning. Consider adding package "
                + "integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1\",\"file\":\"\",\"lineNumber\":-1}]}"));
    }

    @Test
    public void shouldReportThanVnfValidationFailed() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(new String[]{
            "--product", "onap-dublin",
            "csar-validate",
            "--format", "json",
            "--csar", absoluteFilePath("VoLTE.csar")});

        // when
        cli.handle();

        // then
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        verifyThatOperation(onapCommandResult, OPERATION_STATUS_FAILED);
        verifyThatXRulesFails(onapCommandResult, 7);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }


    @Test
    public void shouldReportThatPnfValidationFailedWhenCsarDoNotHaveCertificate_allOtherRulesShouldPass() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(new String[]{
            "--product", "onap-dublin",
            "csar-validate",
            "--format", "json",
            "--pnf",
            "--csar", absoluteFilePath("pnf/r972082/validFile.csar")});
        // when
        cli.handle();

        // then
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        verifyThatOperation(onapCommandResult, OPERATION_STATUS_FAILED);
        verifyThatXRulesFails(onapCommandResult, 1);
        verifyThatRuleFails(onapCommandResult, NO_CERTIFICATE_RULE);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportThatPnfValidationFailedWhenZipDoNotHaveCertificate_allOtherRulesShouldPass() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(new String[]{
            "--product", "onap-dublin",
            "csar-validate",
            "--format", "json",
            "--pnf",
            "--csar", absoluteFilePath("pnf/signed-package-valid-signature.zip")});

        // when
        cli.handle();

        // then
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        verifyThatOperation(onapCommandResult, OPERATION_STATUS_FAILED);
        verifyThatXRulesFails(onapCommandResult, 1);
        verifyThatRuleFails(onapCommandResult, NO_CERTIFICATE_RULE);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    private void verifyThatXRulesFails(OnapCommandResult onapCommandResult, int noOfRules) {
        verifyDataOccurrenceInResult(onapCommandResult, "\"passed\":false", noOfRules);
    }

    private void verifyThatRuleFails(OnapCommandResult onapCommandResult, String nameOfRule) {
        final String value = String.format("\"passed\":false,\"vnfreqName\":\"%s\"", nameOfRule);
        verifyDataOccurrenceInResult(onapCommandResult, value, 1);
    }

    private void verifyDataOccurrenceInResult(OnapCommandResult onapCommandResult, String data, int dataOccurrence) {
        final String json = getResultValue(onapCommandResult, "results");
        int occurrence = StringUtils.countMatches(json, data);
        Assertions.assertThat(occurrence).isEqualTo(dataOccurrence);
    }

    private void verifyThatOperation(OnapCommandResult onapCommandResult, String operationStatus) {
        final String value = getResultValue(onapCommandResult, "criteria");
        Assertions.assertThat(value).isEqualTo(operationStatus);
    }

    private String getResultValue(OnapCommandResult onapCommandResult, String attributeName) {
        final OnapCommandResultAttribute results = getOnapCommandResultAttribute(onapCommandResult, attributeName);
        return results.getValues().get(0);
    }

    private OnapCommandResultAttribute getOnapCommandResultAttribute(OnapCommandResult onapCommandResult, String name) {
        return onapCommandResult.getRecords().stream()
                .filter(it -> it.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Unable to find '%s' attribute!", name)));
    }

    private void verifyThatOperationFinishedWithoutAnyError(OnapCliWrapper cli) {
        assertEquals(0, cli.getExitCode());
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
}
