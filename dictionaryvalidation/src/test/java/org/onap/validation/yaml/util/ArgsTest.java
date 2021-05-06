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
package org.onap.validation.yaml.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ArgsTest {

    @Test
    void shouldThrowAnExceptionWhenArgIsNotAvailable() {
        Args args = new Args(List.of());
        Assertions.assertThatThrownBy(() -> args.getArg(0)).hasMessage("Argument with index 0 is not available!");
    }

    @Test
    void shouldReturnArgumentForGivenIndex() {
        Args args = new Args(List.of("one","two", "three"));
        Assertions.assertThat(args.getArg(0)).isEqualTo("one");
        Assertions.assertThat(args.getArg(1)).isEqualTo("two");
        Assertions.assertThat(args.getArg(2)).isEqualTo("three");
    }
}
