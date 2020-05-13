/*
 * Copyright 2019 Nordix
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.configureTestCase;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.convertToMessagesList;

import java.net.URISyntaxException;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;

public class VTPValidateCSARR972082IntegrationTest {

    private static final boolean IS_PNF = true;
    private static final String VTP_VALIDATE_CSAR_R_972082_YAML = "vtp-validate-csar-r972082.yaml";
    private static final String PNF_R_972082 = "pnf/r972082/";
    private VTPValidateCSARR972082 testCase;

    @Before
    public void setUp() {
        testCase = new VTPValidateCSARR972082();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R972082");
    }

    @Test
    public void shouldReturnNoErrorOnCorrectCsarFile() throws Exception {
        // given
        configureTestCase(testCase, PNF_R_972082 + "validFile.csar", VTP_VALIDATE_CSAR_R_972082_YAML, IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReportThatEntryHasInvalidPathWhenYamlFileIsNotPresent() throws Exception {
        // given
        configureTestCase(testCase, PNF_R_972082 + "missingYamlFileReferedInSourceSessionOfManifest.csar",
            VTP_VALIDATE_CSAR_R_972082_YAML,
            IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
            "Invalid. Entry [Source under onap_pnf_sw_information has invalid 'Files/pnf-sw-information/pnf-sw-information.yaml' path]"
        );
    }

    @Test
    public void shouldReportThatMandatoryNonManoArtifactSetEntryHasNotAllFields_() throws Exception {
        // given
        configureTestCase(testCase, PNF_R_972082 + "missingFieldsInNonManoArtifactManifest.csar",
            VTP_VALIDATE_CSAR_R_972082_YAML, IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
            "Missing. Entry [[onap_pnf_sw_information]]"
        );
    }

    @Test
    public void shouldReportMissingPnfSoftwareVersionInYamlFile() throws Exception {
        // given
        configureTestCase(testCase, PNF_R_972082 + "missingPnfSoftwareVersionInYamlFile.csar",
            VTP_VALIDATE_CSAR_R_972082_YAML, IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
            "Missing. Entry [pnf_software_version in Files/pnf-sw-information/pnf-sw-information.yaml]"
        );
    }

    @Test
    public void shouldReportMissingSourceElementUnderAttribute() throws OnapCommandException, URISyntaxException {
        // given
        configureTestCase(testCase, PNF_R_972082 + "missingSourceElementUnderAttributeError.csar",
                VTP_VALIDATE_CSAR_R_972082_YAML, IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [Source under onap_pnf_sw_information]"
        );
    }

    @Test
    public void shouldReportInvalidYamlStructure() throws OnapCommandException, URISyntaxException {
        // given
        configureTestCase(testCase, PNF_R_972082 + "invalidYamlStructure.csar",
                VTP_VALIDATE_CSAR_R_972082_YAML, IS_PNF);

        // when
        testCase.execute();

        // then
        final List<CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(convertToMessagesList(errors)).contains(
                "Invalid. Yaml file Files/pnf-sw-information/pnf-sw-information.yaml is invalid"
        );
    }
}