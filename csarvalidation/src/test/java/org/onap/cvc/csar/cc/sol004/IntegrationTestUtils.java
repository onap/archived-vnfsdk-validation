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
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandInvalidParameterValue;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cli.fw.output.OnapCommandResultAttribute;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.VTPValidatePnfCSARBase;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IntegrationTestUtils {

    private IntegrationTestUtils() {

    }

    public static String absoluteFilePath(String relativeFilePath) throws URISyntaxException {
        return VTPValidatePnfCSARBase.class.getClassLoader().getResource(relativeFilePath)
                .toURI().getPath();
    }

    static void configureTestCase(VTPValidatePnfCSARBase testCase, String fileName) throws OnapCommandException, URISyntaxException {
        configureCommandAttributes(testCase);

        testCase.initializeSchema("vtp-validate-csar-r146092.yaml");

        configurePathToCsar(testCase, fileName);
    }

    private static void configureCommandAttributes(VTPValidatePnfCSARBase testCase) {
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

    private static void configurePathToCsar(VTPValidatePnfCSARBase testCase, String fileName) throws URISyntaxException, OnapCommandInvalidParameterValue {
        String pathToFile = absoluteFilePath(fileName);
        Set<OnapCommandParameter> parameters = testCase.getParameters();
        OnapCommandParameter csar = parameters.stream().filter(op -> op.getName().equals("csar")).findFirst().get();
        csar.setValue(pathToFile);
    }

    static List<String> convertToMessagesList(List<CSARArchive.CSARError> errors) {
        return errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList());
    }
}
