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


import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;

import java.util.Objects;

@OnapCommandSchema(schema = "vtp-validate-csar-r293901.yaml")
public class VTPValidateCSARR293901 extends VTPValidateCSARBase {

    private static final int UNKNOWN_LINE_NUMBER = -1;

    @Override
    protected void validateCSAR(CSARArchive csar) {
        final CSARArchive.TOSCAMeta toscaMeta = csar.getToscaMeta();
        String entryManifestMf = toscaMeta.getEntryManifestMf();

        validateToscaMetaValue(entryManifestMf, "ETSI-Entry-Manifest");
        validateToscaMetaValue(toscaMeta.getEntryChangeLog(), "ETSI-Entry-Change-Log");
    }

    private void validateToscaMetaValue(String entryManifestMf, String toscaMetaAttributeName) {
        if (Objects.isNull(entryManifestMf)) {
            this.errors.add(new PnfCSARErrorEntryMissing(
                    toscaMetaAttributeName,
                    CSARArchive.TOSCA_METADATA_TOSCA_META,
                    UNKNOWN_LINE_NUMBER)
            );
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R293901";
    }


}
