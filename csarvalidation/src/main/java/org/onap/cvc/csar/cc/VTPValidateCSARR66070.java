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

package org.onap.cvc.csar.cc;

import java.util.ArrayList;
import java.util.List;

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R-66070: VNF provider details
 */
@OnapCommandSchema(schema = "vtp-validate-csar-r66070.yaml")
public class VTPValidateCSARR66070 extends OnapCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR66070.class);

    public static class CSARErrorEntryVNFProviderDetailsNotFound extends CSARErrorEntryMissing {
        public CSARErrorEntryVNFProviderDetailsNotFound() {
            super(
                    "VNF Vendor details",
                    CSARArchive.TOSCA_Metadata + " or " + CSARArchive.TOSCA_Metadata__TOSCA_Meta__Entry_Definitions + " file",
                    -1,
                    "The VNF Package MUST include VNF Identification Data to uniquely identify the"
                    + " resource for a given VNF provider. The identification data must include: "
                    + "an identifier for the VNF, the name of the VNF as was given by the VNF provider, "
                    + "VNF description, VNF provider, and version.");
            this.setSubCode("r66070-0x1000");
        }
    }

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();
        List<CSARError> errors = new ArrayList<>();
        //execute
        try {
            CSARArchive csar = new CSARArchive();
            csar.init(path);
            csar.parse();

            if (csar.getVendorName() == null ||
                    csar.getVersion() == null) {
                errors.add(new CSARErrorEntryVNFProviderDetailsNotFound());
            }
            //do the validation
            csar.cleanup();
        } catch (Exception e) {
            LOG.error("R-66070: ", e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }

        this.getResult().setOutput(errors);

        //set the result
        for (CSARError e: errors) {
            this.getResult().getRecordsMap().get("code").getValues().add(e.getCode());
            this.getResult().getRecordsMap().get("message").getValues().add(e.getMessage());
            this.getResult().getRecordsMap().get("file").getValues().add(e.getFile());
            this.getResult().getRecordsMap().get("line-no").getValues().add(Integer.toString(e.getLineNumber()));
        }
   }
}
