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

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class OutputFilePathGeneratorTest {

    private final OutputFilePathGenerator filePathGenerator = new OutputFilePathGenerator();

    @Test
    void shouldReturnPathToFileWithResponse() {
        // given
        final Path pathToYaml = Path.of("/some/path/PMDictionary.yaml");
        final Path expected = Path.of("/some/path/PMDictionary-validation-results.json");

        // when
        final Path actual = filePathGenerator.responsePathFor(pathToYaml);

        // then
        assertThat(actual).isEqualTo(expected);
    }

}
