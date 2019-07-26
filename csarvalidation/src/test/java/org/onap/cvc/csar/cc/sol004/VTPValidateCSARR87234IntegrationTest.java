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

package org.onap.cvc.csar.cc.sol004;

import org.junit.Before;
import org.junit.Test;
import org.onap.cvc.csar.CSARArchive;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;


public class VTPValidateCSARR87234IntegrationTest {

    private static final boolean IS_PNF = true;
    private VTPValidateCSARR87234 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR87234();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R87234");
    }


    @Test
    public void shouldReportThatToscaMetadataDirectoryIsNotPresent() throws Exception {
        // given
        configureTestCase(testCase, "pnf/noToscaMetaFile.csar", "vtp-validate-csar-r87234.yaml", IS_PNF);

        // when
        testCase.execute();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(5);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid value. Entry [CSAR Mode]. Valid values are [WITH_TOSCA_META_DIR]. Only csar package with TOSCA-Metadata directory is supported. Unable to find TOSCA.meta file in TOSCA-metadata directory."
        );
    }

}