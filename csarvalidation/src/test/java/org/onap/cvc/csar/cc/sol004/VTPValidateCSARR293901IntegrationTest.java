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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.onap.cli.fw.error.OnapCommandInvalidParameterValue;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cli.fw.output.OnapCommandResultAttribute;
import org.onap.cvc.csar.CSARArchive;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class VTPValidateCSARR293901IntegrationTest {

    private VTPValidateCSARR293901 testCase;

    @Before
    public void setUp(){
        testCase = new VTPValidateCSARR293901();
    }

    @Test
    public void shouldReturnProperRequestNumber() {
        assertThat(testCase.getVnfReqsNo()).isEqualTo("R293901");
    }

    @Test
    public void shouldReportThatMandatoryEntriesAreNotAvailable() throws Exception {
        // given
        configureTestCase(testCase, "pnf/noMandatoryEntriesInTOSCAMeta.csar");

        // when
        testCase.run();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(convertToMessagesList(errors)).contains(
                "Missing. Entry [Entry-Definitions]",
                "Missing. Entry [ETSI-Entry-Manifest]",
                "Missing. Entry [ETSI-Entry-Change-Log]"
        );
    }

    @Test
    public void shouldDoNotReportAnyErrorWhenAllMandatoryEntriesWereDefined() throws Exception {
        // given
        configureTestCase(testCase, "pnf/allMandatoryEntriesDefinedInTOSCAMeta.csar");

        // when
        testCase.run();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReportAnyErrorWhneThereIsNoTOSCAMetaFileInTOSCADirectory() throws Exception {
        // given
        configureTestCase(testCase, "pnf/noTOSCAMetaInTOSCADirectory.csar");

        // when
        testCase.run();

        // then
        List<CSARArchive.CSARError> errors = testCase.getErrors();
        assertThat(errors.size()).isEqualTo(4);
        assertThat(convertToMessagesList(testCase.getErrors())).contains(
                "Missing. Entry [Definition YAML]",
                "Ignored. Entry [TOSCA-Metadata]",
                "Missing. Entry [ETSI-Entry-Manifest]",
                "Missing. Entry [ETSI-Entry-Change-Log]"
        );
    }

    private String resolvePathToFile(String s) throws URISyntaxException {
        return VTPValidateCSARR293901IntegrationTest.class.getClassLoader().getResource(s)
                .toURI().getPath();
    }

    private void configureTestCase(VTPValidateCSARR293901 testCase, String fileName) throws OnapCommandInvalidParameterValue, URISyntaxException {
        String pathToFile = resolvePathToFile(fileName);
        OnapCommandParameter param = new OnapCommandParameter();
        param.setName("csar");
        param.setValue(pathToFile);
        Set<OnapCommandParameter> parameters = Sets.newHashSet(param);
        testCase.setParameters(parameters);

        OnapCommandResult onapCommandResult = new OnapCommandResult();
        OnapCommandResultAttribute onapCommandResultAttributeCode = new OnapCommandResultAttribute();
        onapCommandResultAttributeCode.setName("code");
        OnapCommandResultAttribute onapCommandResultAttributeMessage = new OnapCommandResultAttribute();
        onapCommandResultAttributeMessage.setName("message");
        OnapCommandResultAttribute onapCommandResultAttributeFile = new OnapCommandResultAttribute();
        onapCommandResultAttributeFile.setName("file");
        OnapCommandResultAttribute onapCommandResultAttributeLineNo = new OnapCommandResultAttribute();
        onapCommandResultAttributeLineNo.setName("line-no");
        List<OnapCommandResultAttribute> records = Lists.newArrayList(onapCommandResultAttributeCode, onapCommandResultAttributeMessage, onapCommandResultAttributeFile, onapCommandResultAttributeLineNo);
        onapCommandResult.setRecords(records);
        testCase.setResult(onapCommandResult);
    }

    private List<String> convertToMessagesList(List<CSARArchive.CSARError> errors) {
        return errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList());
    }


}