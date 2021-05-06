/*
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
import org.onap.functional.util.OnapCliWrapper;
import org.onap.validation.rule.DictionaryValidate;
import org.onap.validation.rule.DictionaryValidateResponse;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.functional.util.ValidationUtility.OPERATION_STATUS_FAILED;
import static org.onap.functional.util.ValidationUtility.OPERATION_STATUS_PASS;
import static org.onap.functional.util.ValidationUtility.getCliCommandValidationResult;
import static org.onap.functional.util.ValidationUtility.verifyThatOperationFinishedWithError;
import static org.onap.functional.util.ValidationUtility.verifyThatOperationFinishedWithoutAnyError;


public class DictionaryValidateRuleFunctionalTest {


    private static final String YAML_SCHEMA_SIMPLE_VALID_SCHEMA_YAML = "yaml_schema/Simple_Valid_Schema.yaml";
    private static final String YAML_CUSTOM_SCHEMA_SIMPLE_VALID_SCHEMA_YAML = "yaml_schema/Custom_Valid_Dictionary.yaml";
    private static final String YAML_CUSTOM_SCHEMA_SIMPLE_INVALID_SCHEMA_YAML = "yaml_schema/Custom_Invalid_Dictionary.yaml";
    private static final String NO_ERRORS = "[]";
    private static final String YAML_SCHEMA_PM_DICTIONARY_YAML = "yaml_schema/PM_Dictionary.yaml";
    private static final String PATH_TO_NON_EXITING_FILE_YAML = "path/to/nonExitingFile.yaml";
    private static final String NOT_YAML_FILE_TXT = "Not_Yaml_File.txt";

    @Test
    public void shouldSuccessfullyValidatePMDictionaryYamlFile() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(YAML_SCHEMA_SIMPLE_VALID_SCHEMA_YAML));

        // when
        cli.handle();

        // then
        final DictionaryValidateResponse result = getCliCommandValidationResult(cli, DictionaryValidateResponse.class);

        assertThat(result.getFile()).contains(YAML_SCHEMA_SIMPLE_VALID_SCHEMA_YAML);
        assertThat(result.getPlatform()).contains(DictionaryValidateResponse.PLATFORM_VTP_1_0);
        assertThat(result.getContact()).contains(DictionaryValidateResponse.ONAP_DISCUSS_LISTS_ONAP_ORG);
        assertThat(result.getCriteria()).isEqualTo(OPERATION_STATUS_PASS);
        assertThat(result.getErrors()).isEqualTo(NO_ERRORS);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldSuccessfullyValidateCustomDictionaryYamlFile() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(YAML_CUSTOM_SCHEMA_SIMPLE_VALID_SCHEMA_YAML));

        // when
        cli.handle();

        // then
        final DictionaryValidateResponse result = getCliCommandValidationResult(cli, DictionaryValidateResponse.class);

        assertThat(result.getFile()).contains(YAML_CUSTOM_SCHEMA_SIMPLE_VALID_SCHEMA_YAML);
        assertThat(result.getPlatform()).contains(DictionaryValidateResponse.PLATFORM_VTP_1_0);
        assertThat(result.getContact()).contains(DictionaryValidateResponse.ONAP_DISCUSS_LISTS_ONAP_ORG);
        assertThat(result.getCriteria()).isEqualTo(OPERATION_STATUS_PASS);
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldFailWhileValidatingCustomDictionaryYamlFileWithEerror() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(YAML_CUSTOM_SCHEMA_SIMPLE_INVALID_SCHEMA_YAML));

        // when
        cli.handle();

        // then
        final DictionaryValidateResponse result = getCliCommandValidationResult(cli, DictionaryValidateResponse.class);

        assertThat(result.getFile()).contains(YAML_CUSTOM_SCHEMA_SIMPLE_INVALID_SCHEMA_YAML);
        assertThat(result.getPlatform()).contains(DictionaryValidateResponse.PLATFORM_VTP_1_0);
        assertThat(result.getContact()).contains(DictionaryValidateResponse.ONAP_DISCUSS_LISTS_ONAP_ORG);
        assertThat(result.getCriteria()).isEqualTo(OPERATION_STATUS_FAILED);
        final String errors = result.getErrors();
        assertThat(errors).contains(
            "Value(s) is/are not in array of accepted values.",
            "accepted value(s):  [X, Y, Z]",
            "accepted value(s):  [3, 30, 33]",
            "Key not found: field2"
        );
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldFailsWhenPMDictionaryYamlFileHasSomeErrors() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(YAML_SCHEMA_PM_DICTIONARY_YAML));

        // when
        cli.handle();

        // then
        final DictionaryValidateResponse result = getCliCommandValidationResult(cli, DictionaryValidateResponse.class);

        assertThat(result.getFile()).contains(YAML_SCHEMA_PM_DICTIONARY_YAML);
        assertThat(result.getPlatform()).contains(DictionaryValidateResponse.PLATFORM_VTP_1_0);
        assertThat(result.getContact()).contains(DictionaryValidateResponse.ONAP_DISCUSS_LISTS_ONAP_ORG);
        assertThat(result.getCriteria()).isEqualTo(OPERATION_STATUS_FAILED);
        final String errors = result.getErrors();
        assertThat(errors).contains(
                "Value(s) is/are not in array of accepted values.",
                "accepted value(s):  [float, uint32, uint64]",
                "Key not found: measChangeType",
                "accepted value(s):  [X, Y, Z]"
        );
        verifyThatOperationFinishedWithoutAnyError(cli);
    }

    @Test
    public void shouldReportAnErrorWhenYamlFileDoesNotExist() {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(new String[]{PATH_TO_NON_EXITING_FILE_YAML});

        // when
        cli.handle();

        verifyThatOperationFinishedWithError(cli);
    }


    @Test
    public void shouldReportThatValidationFailedWithInternalError() throws URISyntaxException {
        // given
        OnapCliWrapper cli = new OnapCliWrapper(createPnfValidationRequestInfo(NOT_YAML_FILE_TXT));

        // when
        cli.handle();

        // then
        final DictionaryValidateResponse result = getCliCommandValidationResult(cli, DictionaryValidateResponse.class);

        assertThat(result.getFile()).contains(NOT_YAML_FILE_TXT);
        assertThat(result.getPlatform()).contains(DictionaryValidateResponse.PLATFORM_VTP_1_0);
        assertThat(result.getContact()).contains(DictionaryValidateResponse.ONAP_DISCUSS_LISTS_ONAP_ORG);
        assertThat(result.getCriteria()).isEqualTo(OPERATION_STATUS_FAILED);
        assertThat(result.getErrors()).contains("Provided yaml file has invalid structure!");
        verifyThatOperationFinishedWithoutAnyError(cli);
    }


    private String[] createPnfValidationRequestInfo(String yamlPath) throws URISyntaxException {
        return new String[]{
                "--product", "onap-honolulu",
                "dictionary-validate",
                "--format", "json",
                "--yaml", absoluteFilePath(yamlPath)
        };
    }


    public static String absoluteFilePath(String relativeFilePath) throws URISyntaxException {
        return DictionaryValidate.class.getClassLoader().getResource(relativeFilePath)
                .toURI().getPath();
    }
}
