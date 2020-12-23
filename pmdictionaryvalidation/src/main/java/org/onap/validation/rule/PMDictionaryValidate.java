/*
 * Copyright 2019 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.validation.rule;

import com.google.gson.Gson;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.output.OnapCommandResultType;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.validation.cli.command.validate.ResponseModel;
import org.onap.validation.cli.command.validate.ResponseModel.ResponseStatus;
import org.onap.validation.cli.command.validate.ToJsonConverter;
import org.onap.validation.cli.command.validate.ToResponseModelConverter;
import org.onap.validation.cli.command.validate.ValidatePmDictionaryYamlCommand;
import org.onap.validation.cli.core.CommandException;
import org.onap.validation.cli.core.CommandResponse;
import org.onap.validation.yaml.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Validates CSAR
 */
@OnapCommandSchema(schema = "pm-dictionary-validate.yaml")
public class PMDictionaryValidate extends OnapCommand {

    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAILED = "FAILED";
    public static final String PARAM_IN_YAML = "yaml";
    public static final String PARAM_OUT_FILE = "file";
    public static final String PARAM_OUT_DATE = "date";
    public static final String PARAM_OUT_CRITERIA = "criteria";
    public static final String PARAM_OUT_ERRORS = "errors";
    private final Gson gson = new Gson();

    private static final Logger logger = LoggerFactory.getLogger(PMDictionaryValidate.class);

    protected void run() throws OnapCommandException {

        final Date timestamp = new Date();
        final String yamlPath = (String) getParametersMap().get(PARAM_IN_YAML).getValue();

        try {
            final ResponseModel responseModel = executeValidation(yamlPath);
            handleResponse(responseModel, timestamp);
        } catch (CommandException e) {
            handleError(timestamp, yamlPath, e);
        }
    }

    private void handleResponse(ResponseModel responseModel, Date timestamp) {
        setOclipResponse(responseModel.getFile(),
                timestamp,
                getCriteria(responseModel),
                transformToJson(responseModel.getErrors())
        );
    }

    private void handleError(Date validationTimestamp, String path, CommandException e) {
        setOclipResponse(path,
                validationTimestamp,
                VALIDATION_FAILED,
                transformToJson(e.getMessage())
        );
        logger.error("Internal application error", e);
    }

    private String getCriteria(ResponseModel responseModel) {
        return responseModel.getStatus().equals(ResponseStatus.PASS) ? VALIDATION_PASS : VALIDATION_FAILED;
    }

    private <T> String transformToJson(T data) {
        return gson.toJson(data);
    }

    private void setOclipResponse(String pathToFile, Date timestamp, String criteria, String errors) {
        final PMDictionaryValidateResponse pmDictionaryValidateResponse = new PMDictionaryValidateResponse(
                pathToFile,
                timestamp.toString(),
                criteria,
                errors
        );
        setOclipResponse(pmDictionaryValidateResponse);
    }

    private void setOclipResponse(PMDictionaryValidateResponse pmDictionaryValidateResponse) {
        this.getResult().getRecordsMap().get(PARAM_OUT_FILE).getValues().add(pmDictionaryValidateResponse.getFile());
        this.getResult().getRecordsMap().get(PARAM_OUT_DATE).getValues().add(pmDictionaryValidateResponse.getDate());
        this.getResult().getRecordsMap().get(PARAM_OUT_CRITERIA).getValues().add(pmDictionaryValidateResponse.getCriteria());
        this.getResult().getRecordsMap().get(PARAM_OUT_ERRORS).getValues().add(pmDictionaryValidateResponse.getErrors());
        this.getResult().setOutput(transformToJson(pmDictionaryValidateResponse));
        this.getResult().setType(OnapCommandResultType.TEXT);
    }

    private ResponseModel executeValidation(String path) throws CommandException {
        final ValidatePmDictionaryYamlCommand validation = new ValidatePmDictionaryYamlCommand(new ToJsonConverter());
        final CommandResponse<String> commandResponse = validation.execute(new Args(List.of(path)));
        final String result = commandResponse.getResult();

        return ToResponseModelConverter.toModel(result);
    }
}
