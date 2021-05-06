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
package org.onap.validation.cli.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.validation.cli.command.validate.ResponseStorage;
import org.onap.validation.yaml.util.Args;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CliTest {
    @Mock
    private Console console;

    @Mock
    private ResponseStorage responseStorage;

    @Mock
    private Command<String> validationCommand;

    private Cli<String> cli;

    @BeforeEach
    void setUp() {
        cli = new Cli<>(console, responseStorage);
    }

    @Test
    void shouldExecuteCommandWithoutAnyError() throws CommandException {

        // given
        Args args = new Args(List.of());
        when(validationCommand.execute(args)).thenReturn(
                new CommandResponse<>("{ 'errors': [] }", CommandResponse.CommandStatus.PASS)
        );

        // when
        final int exitCode = cli.run(args, validationCommand);

        // then
        assertThat(exitCode).isEqualTo(Cli.PASS_STATUS_CODE);
        verify(console).info("{ 'errors': [] }");
    }

    @Test
    void shouldHandleErrorReportedByCommand() throws CommandException {

        // given
        Args args = new Args(List.of());
        final CommandException commandException = new CommandException("It should be reported");
        Mockito.doThrow(commandException).when(validationCommand).execute(args);

        // when
        final int exitCode = cli.run(args, validationCommand);

        // then
        assertThat(exitCode).isEqualTo(Cli.INTERNAL_ERROR_STATUS_CODE);
        verify(console).error(commandException);
    }

    @Test
    void shouldHandleRuntimeException() throws CommandException {

        // given
        Args args = new Args(List.of());
        final RuntimeException commandException = new RuntimeException();
        Mockito.doThrow(commandException).when(validationCommand).execute(args);

        // when
        final int exitCode = cli.run(args, validationCommand);

        // then
        assertThat(exitCode).isEqualTo(Cli.INTERNAL_ERROR_STATUS_CODE);
        verify(console).error(commandException);
    }
}
