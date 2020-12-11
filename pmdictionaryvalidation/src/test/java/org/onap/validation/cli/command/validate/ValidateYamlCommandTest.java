/*
 *Copyright 2020 Nokia
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
package org.onap.validation.cli.command.validate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.validation.cli.core.CommandException;
import org.onap.validation.cli.core.CommandResponse;
import org.onap.validation.yaml.YamlLoadingUtils;
import org.onap.validation.yaml.util.Args;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.validation.cli.util.ToResponseModelConverter.toModel;

@ExtendWith(MockitoExtension.class)
class ValidateYamlCommandTest {

    private final ToJsonConverter toJsonConverter = new ToJsonConverter();
    private ValidatePmDictionaryYamlCommand validateYamlCommand;

    @BeforeEach
    void setUp() {
        this.validateYamlCommand = new ValidatePmDictionaryYamlCommand(toJsonConverter);
    }

    @Test
    void shouldReportAnErrorWhenFileDoesNotExist() {
        // given
        Args args = new Args(List.of("/invalid/filePath/toValidate.yaml"));

        // when
        assertThatThrownBy(() -> validateYamlCommand.execute(args)).hasMessage("File '/invalid/filePath/toValidate.yaml' does not exist!");
    }

    @Test
    void shouldReportAnErrorWhenPathToFileWasNotPass() {
        // given
        Args args = new Args(List.of());

        // when
        assertThatThrownBy(() -> validateYamlCommand.execute(args)).hasMessage("Command argument is missing: pls provide a path to file");
    }

    @Test
    void shouldValidateFileWithoutAnyError() throws CommandException {
        // given
        final String path = YamlLoadingUtils.getUrlForGivenPath(YamlLoadingUtils.PATH_TO_SIMPLE_VALID_SCHEMA).getPath();
        Args args = new Args(List.of(path));

        // when
        final CommandResponse<String> commandResponse = validateYamlCommand.execute(args);

        // then
        assertThat(commandResponse.getCommandStatus()).isEqualTo(CommandResponse.CommandStatus.PASS);
        ResponseModel actual = toModel(commandResponse.getResult());
        assertThat(actual.getFile()).isEqualTo(path);
        assertThat(actual.getStatus()).isEqualTo(ResponseModel.ResponseStatus.PASS);
        assertThat(actual.getErrors()).isEmpty();
    }

    @Test
    void shouldReportThatFileHasSomeErrors() throws CommandException {
        // given
        final String path = YamlLoadingUtils.getUrlForGivenPath(YamlLoadingUtils.PATH_TO_YAML_WITH_WRONG_VALUES).getPath();
        Args args = new Args(List.of(path));

        // when
        final CommandResponse<String> commandResponse = validateYamlCommand.execute(args);

        // then
        assertThat(commandResponse.getCommandStatus()).isEqualTo(CommandResponse.CommandStatus.FAILED);
        ResponseModel actual = toModel(commandResponse.getResult());
        assertThat(actual.getFile()).isEqualTo(path);
        assertThat(actual.getStatus()).isEqualTo(ResponseModel.ResponseStatus.FAILED);
        assertThat(actual.getErrors()).hasSize(4);
    }
}
