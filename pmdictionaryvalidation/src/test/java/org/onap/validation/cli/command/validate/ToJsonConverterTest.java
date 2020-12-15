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

import org.junit.jupiter.api.Test;
import org.onap.validation.yaml.error.YamlDocumentValidationError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.validation.cli.util.ToResponseModelConverter.toModel;

class ToJsonConverterTest {

    private static final String PATH_TO_TEST_YAML = "/path/to/test.yaml";
    private final ToJsonConverter toJsonConverter = new ToJsonConverter();

    @Test
    void shouldConvertEmptyArray() {
        // given
        final ResponseModel responseModel = new ResponseModel(PATH_TO_TEST_YAML,
                ResponseModel.ResponseStatus.PASS,
                List.of());

        // when
        String json = toJsonConverter.convert(responseModel);

        // then
        ResponseModel actual = toModel(json);
        assertThat(actual.getFile()).isEqualTo(PATH_TO_TEST_YAML);
        assertThat(actual.getStatus()).isEqualTo(ResponseModel.ResponseStatus.PASS);
        assertThat(actual.getErrors()).isEmpty();
    }


    @Test
    void shouldConvertListOfErrors() {
        // given
        final ResponseModel responseModel = new ResponseModel(PATH_TO_TEST_YAML,
                ResponseModel.ResponseStatus.FAILED,
                List.of(
                        new YamlDocumentValidationError(1, PATH_TO_TEST_YAML, "error1"),
                        new YamlDocumentValidationError(2, PATH_TO_TEST_YAML, "error2")));

        // when
        String json = toJsonConverter.convert(responseModel);

        // then

        ResponseModel actual = toModel(json);
        assertThat(actual.getFile()).isEqualTo(PATH_TO_TEST_YAML);
        assertThat(actual.getStatus()).isEqualTo(ResponseModel.ResponseStatus.FAILED);
        assertThat(actual.getErrors()).hasSize(2);
    }
}
