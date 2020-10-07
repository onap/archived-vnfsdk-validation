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

package org.onap.functional;

import org.junit.Test;
import org.onap.functional.cli.OnapCliValidationResponseWrapper;
import org.onap.functional.cli.OnapCliWrapper;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;
import static org.onap.functional.CsarValidationUtility.OPERATION_STATUS_FAILED;
import static org.onap.functional.CsarValidationUtility.getCliCommandValidationResult;
import static org.onap.functional.CsarValidationUtility.ruleHaveOneOfCodes;
import static org.onap.functional.CsarValidationUtility.verifyThatOperationFinishedWithoutAnyError;

public class VnfValidationFunctionalTest {


    @Test
    public void shouldReportThanVnfValidationFailed() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createVnfValidationRequestInfo("VoLTE.csar"));

        // when
        cli.handle();

        // then
        final OnapCliValidationResponseWrapper result = getCliCommandValidationResult(cli);
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

    private String[] createVnfValidationRequestInfo(String csarPath) throws URISyntaxException {
        return new String[]{
            "--product", "onap-dublin",
            "csar-validate",
            "--format", "json",
            "--csar", absoluteFilePath(csarPath)
        };
    }

}
