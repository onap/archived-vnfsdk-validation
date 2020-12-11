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

import org.onap.validation.cli.command.validate.ResponseStorage;
import org.onap.validation.cli.core.CommandResponse.CommandStatus;
import org.onap.validation.yaml.util.Args;

import java.nio.file.Path;

public class Cli<T> {

    public static final int INTERNAL_ERROR_STATUS_CODE = 2;
    public static final int FAILED_STATUS_CODE = 1;
    public static final int PASS_STATUS_CODE = 0;

    public static final String APPLICATION_INTERNAL_ERROR_MSG = "\n# Application fails with internal error.";
    public static final String APPLICATION_EXIT_SUCCESSFULLY_MSG = "\n# Application exits successfully.";

    private final Console console;
    private final ResponseStorage responseStorage;

    public Cli(Console console, ResponseStorage responseStorage) {
        this.console = console;
        this.responseStorage = responseStorage;
    }

    public int run(Args args, Command<T> command) {
        try {
            final CommandResponse<T> commandResponse = processCommand(args, command);

            logResultInfo(commandResponse.getResult());
            storeResult(args, command, commandResponse.getResult());

            this.console.info(APPLICATION_EXIT_SUCCESSFULLY_MSG);

            return resolveCliStatusCodeFor(commandResponse);
        } catch (Exception e) {
            logException(e);
            return INTERNAL_ERROR_STATUS_CODE;
        }
    }

    private int resolveCliStatusCodeFor(CommandResponse<T> commandResponse) {
        return commandResponse.getCommandStatus() == CommandStatus.PASS ? PASS_STATUS_CODE : FAILED_STATUS_CODE;
    }

    private void logException(Exception e) {
        this.console.error("# Command error: ");
        this.console.error(e);
        this.console.error(APPLICATION_INTERNAL_ERROR_MSG);
    }

    private void storeResult(Args args, Command<T> command, T result) throws CommandException, java.io.IOException {
        final Path pathToFileWithResponse = command.getOutputFilePath(args);
        this.responseStorage.store(pathToFileWithResponse, result);
        this.console.info(String.format("%n# Result was stored in a file: '%s'", pathToFileWithResponse));
    }

    private void logResultInfo(T result) {
        this.console.info("\n# Operation result:\n");
        this.console.info(result.toString());
    }

    private CommandResponse<T> processCommand(Args args, Command<T> command) throws CommandException {
        this.console.info(String.format("# Executing a '%s' operation ...", command.getName()));
        final CommandResponse<T> commandResponse = command.execute(args);
        this.console.info("# ... Done.");
        return commandResponse;
    }
}
