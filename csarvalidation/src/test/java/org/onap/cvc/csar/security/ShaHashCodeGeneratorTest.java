/*
 * Copyright 2019 Nokia
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
 *
 */

package org.onap.cvc.csar.security;


import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

public class ShaHashCodeGeneratorTest {

    private ShaHashCodeGenerator shaHashCodeGenerator;

    @Before
    public void setUp(){
        shaHashCodeGenerator = new ShaHashCodeGenerator();
    }

    @Test
    public void shouldGenerateHashCodeSHA256() throws NoSuchAlgorithmException {

        final String hashCode = shaHashCodeGenerator.generateSha256("test".getBytes());

        assertThat(hashCode)
        .isEqualTo(shaHashCodeGenerator.generateSha256("test".getBytes()))
        .isNotEqualTo(shaHashCodeGenerator.generateSha256("Test".getBytes()));
    }

    @Test
    public void shouldGenerateHashCodeSHA512() throws NoSuchAlgorithmException {

        final String hashCode = shaHashCodeGenerator.generateSha512("test".getBytes());

        assertThat(hashCode)
        .isEqualTo(shaHashCodeGenerator.generateSha512("test".getBytes()))
        .isNotEqualTo(shaHashCodeGenerator.generateSha512("Test".getBytes()));
    }
}