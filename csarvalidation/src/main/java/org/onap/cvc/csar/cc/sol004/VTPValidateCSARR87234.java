/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
import org.onap.cvc.csar.CSARArchive.CSARErrorInvalidEntryValue;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;

@OnapCommandSchema(schema = "vtp-validate-csar-r87234.yaml")
public class VTPValidateCSARR87234 extends VTPValidateCSARBase {

    static class CSARErrorInvalidEntryValueNonToscaMetaNotSupported extends CSARErrorInvalidEntryValue {
        CSARErrorInvalidEntryValueNonToscaMetaNotSupported() {
            super("CSAR Mode",
                    "CSAR Archive",
                    "Only csar package with TOSCA-Metadata directory is supported. Unable to find TOSCA.meta file in TOSCA-metadata directory.",
                    CSARArchive.Mode.WITH_TOSCA_META_DIR.name());
            this.setCode("0x1000");
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        if (!csar.getToscaMeta().getMode().name().equalsIgnoreCase(CSARArchive.Mode.WITH_TOSCA_META_DIR.name())) {
            this.errors.add(new CSARErrorInvalidEntryValueNonToscaMetaNotSupported());
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R87234";
    }

}
