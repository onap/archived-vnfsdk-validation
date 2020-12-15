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
package org.onap.validation.cli;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.validation.cli.command.validate.OutputFilePathGenerator;
import org.onap.validation.cli.command.validate.ResponseModel;
import org.onap.validation.cli.command.validate.ResponseStorage;
import org.onap.validation.cli.core.Cli;
import org.onap.validation.cli.core.CommandException;
import org.onap.validation.cli.core.Console;
import org.onap.validation.cli.util.ToResponseModelConverter;
import org.onap.validation.yaml.YamlLoadingUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MainCITest {

    public static final int JSON_RESPONSE_INDEX = 3;
    public static final int OPERATION_INFO_MSG_INDEX = 5;
    public static final int NO_ERROR = 0;
    @Mock
    private Console console;
    @Mock
    private ResponseStorage responseStorage;
    private final OutputFilePathGenerator filePathGenerator = new OutputFilePathGenerator();

    @Test
    void shouldReportThatPathToFileWasNotProvided() {
        // when
        final int statusCode = Main.run(new String[0], console, responseStorage);

        // then
        assertThat(statusCode).isEqualTo(Cli.INTERNAL_ERROR_STATUS_CODE);
        final ArgumentCaptor<CommandException> exceptionCaptor = ArgumentCaptor.forClass(CommandException.class);
        verify(console).error(exceptionCaptor.capture());
        verify(console).error(Cli.APPLICATION_INTERNAL_ERROR_MSG);
        assertThat(exceptionCaptor.getValue().getMessage()).isEqualTo("Command argument is missing: provide a path to file");

    }

    @Test
    void shouldReportThatFileDoseNotExist() throws IOException {
        // when
        final String pathToYaml = "/path/to/nonExisting.yaml";
        final int statusCode = Main.run(new String[]{pathToYaml}, console, new ResponseStorage());

        // then
        assertThat(statusCode).isEqualTo(Cli.INTERNAL_ERROR_STATUS_CODE);
        final ArgumentCaptor<CommandException> exceptionCaptor = ArgumentCaptor.forClass(CommandException.class);
        verify(console).error(exceptionCaptor.capture());
        verify(console).error(Cli.APPLICATION_INTERNAL_ERROR_MSG);
        assertThat(exceptionCaptor.getValue().getMessage()).isEqualTo("File '/path/to/nonExisting.yaml' does not exist!");
        verify(responseStorage, never()).store(eq(Path.of(pathToYaml)), anyString());
    }

    @Test
    void shouldReportThatFileIsBroken() throws IOException {
        // when
        final String pathToFile = YamlLoadingUtils.getUrlForGivenPath("Not_Yaml_File.txt").getPath();
        final int statusCode = Main.run(new String[]{pathToFile}, console, new ResponseStorage());

        // then
        assertThat(statusCode).isEqualTo(Cli.INTERNAL_ERROR_STATUS_CODE);
        final ArgumentCaptor<CommandException> exceptionCaptor = ArgumentCaptor.forClass(CommandException.class);
        verify(console).error(exceptionCaptor.capture());
        verify(console).error(Cli.APPLICATION_INTERNAL_ERROR_MSG);
        assertThat(exceptionCaptor.getValue().getMessage()).isEqualTo("Provided yaml file has invalid structure!");
        verify(responseStorage, never()).store(eq(Path.of(pathToFile)), anyString());
    }

    @Test
    void shouldValidateProperYamlFile() throws IOException {
        // when
        final String path = YamlLoadingUtils.getUrlForGivenPath(YamlLoadingUtils.PATH_TO_SIMPLE_VALID_SCHEMA).getPath();
        final int statusCode = Main.run(new String[]{path}, console, responseStorage);

        // then
        assertThat(statusCode).isEqualTo(Cli.PASS_STATUS_CODE);
        assertThatResponseWasLoggedAtConsole(
                path,
                ResponseModel.ResponseStatus.PASS,
                NO_ERROR
        );
    }

    @Test
    void shouldValidateYamlWithErrors() throws IOException {
        // when
        final String path = YamlLoadingUtils.getUrlForGivenPath(YamlLoadingUtils.PATH_TO_YAML_WITH_WRONG_VALUES).getPath();
        final int statusCode = Main.run(new String[]{path}, console, responseStorage);

        // then
        assertThat(statusCode).isEqualTo(Cli.FAILED_STATUS_CODE);
        assertThatResponseWasLoggedAtConsole(
                path,
                ResponseModel.ResponseStatus.FAILED,
                4
        );
    }

    private void assertThatResponseWasLoggedAtConsole(String pathToFile, ResponseModel.ResponseStatus responseStatus, int expectedErrors) throws IOException {
        ArgumentCaptor<String> consoleInfoCaptor = ArgumentCaptor.forClass(String.class);
        verify(console, times(6)).info(consoleInfoCaptor.capture());
        final List<String> allValues = consoleInfoCaptor.getAllValues();
        final String json = allValues.get(JSON_RESPONSE_INDEX);
        final ResponseModel responseModel = ToResponseModelConverter.toModel(json);
        assertThat(responseModel.getFile()).isEqualTo(pathToFile);
        assertThat(responseModel.getStatus()).isEqualTo(responseStatus);
        assertThat(responseModel.getErrors()).hasSize(expectedErrors);
        assertThat(consoleInfoCaptor.getAllValues().get(OPERATION_INFO_MSG_INDEX)).isEqualTo(Cli.APPLICATION_EXIT_SUCCESSFULLY_MSG);
        verify(responseStorage).store(eq(filePathGenerator.responsePathFor(Path.of(pathToFile))), eq(json));
    }

}
