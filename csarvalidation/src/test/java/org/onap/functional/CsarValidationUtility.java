/**
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

import com.google.gson.Gson;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.functional.cli.OnapCliValidationResponseWrapper;
import org.onap.functional.cli.OnapCliWrapper;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public final class CsarValidationUtility {

    private static final int NOR_ERROR_CODE = 0;
    private static final String UNKNOWN_LINE_NUMBER = "-1";

    private CsarValidationUtility(){}

    public static final String CERTIFICATION_RULE = "r130206";
    public static final String OPERATION_STATUS_FAILED = "FAILED";
    public static final String OPERATION_STATUS_PASS = "PASS";

    public static OnapCliValidationResponseWrapper getCliCommandValidationResult(OnapCliWrapper cli) {
        final OnapCommandResult onapCommandResult = cli.getCommandResult();
        return new Gson().fromJson(onapCommandResult.getOutput().toString(), OnapCliValidationResponseWrapper.class);
    }

    public static OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper createExpectedError(
        String rule, String errorCode, String errorMessage
    ) {
        return new OnapCliValidationResponseWrapper.ValidationResultWrapper.ValidationErrorWrapper(
            rule.toUpperCase(), errorCode,
            errorMessage,
            "", UNKNOWN_LINE_NUMBER
        );
    }

    public static boolean ruleHaveOneOfCodes(String ruleCode, String... codes ) {
        return Arrays.asList(codes).contains(ruleCode);
    }

    public static void verifyThatOperationFinishedWithoutAnyError(OnapCliWrapper cli) {
        assertThat(cli.getExitCode()).isEqualTo(NOR_ERROR_CODE);
    }



}
