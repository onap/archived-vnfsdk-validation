/*
 * Copyright 2020 Nokia.
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

package org.onap.cvc.csar.cc.sol004;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;

import java.util.List;
import java.util.Map;

@OnapCommandSchema(schema = "vtp-validate-csar-r816745.yaml")
public class VTPValidateCSARR816745 extends VTPValidateCSARBase {

    private static final String PM_DICTIONARY = "onap_pm_dictionary";

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        Map<String, Map<String, List<String>>> nonManoFields = csar.getManifest().getNonMano();
        if(nonManoFields.containsKey(PM_DICTIONARY)) {
            validateYamlFile(getLocationOfPmDictionaryFile(nonManoFields));
        }
    }

    private String getLocationOfPmDictionaryFile(Map<String, Map<String, List<String>>> nonManoFields) {
        return nonManoFields.get(PM_DICTIONARY).get("source").get(0);
    }

    private void validateYamlFile(String path) {
        throw  new UnsupportedOperationException("Under development");
    }

    @Override
    protected String getVnfReqsNo() {
        return "R816745";
    }

}
