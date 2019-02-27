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
 * R-27310: Manifest file
 */
@OnapCommandSchema(schema = "vtp-validate-csar-r27310.yaml")
public class VTPValidateCSARR27310 extends OnapCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR27310.class);

    public static class CSARErrorEntryMissingChefArtifactsNotFound extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingChefArtifactsNotFound() {
            super(
                    "cookbooks",
                    CSARArchive.CSAR_Archive,
                    -1,
                    "The VNF Package MUST include all relevant Chef artifacts (roles/cookbooks/recipes) "
                    + "required to execute VNF actions requested by ONAP for loading on appropriate Chef Server.");
            this.setSubCode("r27310-0x1000");
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

            if (!csar.getFileFromCsar("cookbooks").exists()) {
                errors.add(new CSARErrorEntryMissingChefArtifactsNotFound());
            }

            csar.cleanup();
        } catch (Exception e) {
            LOG.error("R-27310: ", e);
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
