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
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;

@OnapCommandSchema(schema = "vtp-validate-csar-r04298.yaml")
public class VTPValidateCSARR04298 extends VTPValidateCSARBase {

    public static class CSARErrorEntryMissingTestFolderNotFound extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingTestFolderNotFound() {
            super("Tests", CSARArchive.CSAR_Archive);
            this.setCode("0x1000");
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {

        if (csar.getTestsFolder() == null) {
            this.errors.add(new CSARErrorEntryMissingTestFolderNotFound());
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R04298";
    }
}
