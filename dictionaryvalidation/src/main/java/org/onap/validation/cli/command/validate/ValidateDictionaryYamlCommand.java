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

import org.onap.validation.cli.core.Command;
import org.onap.validation.cli.core.CommandException;
import org.onap.validation.cli.core.CommandResponse;
import org.onap.validation.yaml.YamlContentValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.util.Args;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ValidateDictionaryYamlCommand implements Command<String> {

    public static final int PATH_TO_FILE_INDEX = 0;
    private final YamlContentValidator yamlContentValidator  = new YamlContentValidator();

    private final ToJsonConverter toJsonConverter;
    private final OutputFilePathGenerator filePathGenerator = new OutputFilePathGenerator();

    public ValidateDictionaryYamlCommand(ToJsonConverter toJsonConverter) {
        this.toJsonConverter = toJsonConverter;
    }

    @Override
    public CommandResponse<String> execute(Args args) throws CommandException {
        final String pathToFile = resolvePathToFile(args);

        try {
            return validate(pathToFile);
        } catch (YamlProcessingException e) {
            throw new CommandException("Provided yaml file has invalid structure!", e);
        }
    }

    @Override
    public String getName() {
        return "Validate Dictionary yaml";
    }

    @Override
    public Path getOutputFilePath(Args args) throws CommandException {
        final String pathToFile = resolvePathToFile(args);
        return this.filePathGenerator.responsePathFor(Path.of(pathToFile));
    }

    private CommandResponse<String> validate(String pathToFile) throws YamlProcessingException {
        final List<YamlDocumentValidationError> errors = yamlContentValidator.validate(pathToFile);
        return new CommandResponse<>(
                toJsonConverter.convert(new ResponseModel(pathToFile, resolveResponseStatus(errors), errors))
                ,resolveCommandStatus(errors)
        );
    }

    private ResponseModel.ResponseStatus resolveResponseStatus(List<YamlDocumentValidationError> errors) {
        return errors.isEmpty() ? ResponseModel.ResponseStatus.PASS : ResponseModel.ResponseStatus.FAILED;
    }

    private CommandResponse.CommandStatus resolveCommandStatus(List<YamlDocumentValidationError> errors) {
        return errors.isEmpty() ? CommandResponse.CommandStatus.PASS : CommandResponse.CommandStatus.FAILED;
    }

    private String resolvePathToFile(Args args) throws CommandException {
        try {
            final String pathToFile = args.getArg(PATH_TO_FILE_INDEX);
            if (!Files.exists(Path.of(pathToFile))) {
                throw new CommandException(String.format("File '%s' does not exist!", pathToFile));
            }
            return pathToFile;
        } catch (IllegalArgumentException ex) {
            throw new CommandException("Command argument is missing: provide a path to file", ex);
        }
    }
}
