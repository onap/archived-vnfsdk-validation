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
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VTPValidateCSARBase extends OnapCommand {
    protected static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARBase.class);

    protected abstract void validateCSAR(CSARArchive csar) throws Exception;

    protected abstract String getVnfReqsNo();

    protected List<CSARError> errors = new ArrayList<>();

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();

        //execute
        try (CSARArchive csar = this.createArchiveInstance()){
            csar.init(path);
            csar.parse();

            errors.addAll(csar.getErrors());

            this.validateCSAR(csar);

        } catch (Exception e) {
            LOG.error(this.getVnfReqsNo() + ": Failed to validate CSAR" , e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }

        for (CSARError e: this.errors) {
            e.setVnfreqNo(this.getVnfReqsNo());
            this.getResult().getRecordsMap().get("code").getValues().add(this.getVnfReqsNo() + "-" + e.getCode());
            this.getResult().getRecordsMap().get("message").getValues().add(e.getMessage());
            this.getResult().getRecordsMap().get("file").getValues().add(e.getFile());
            this.getResult().getRecordsMap().get("line-no").getValues().add(Integer.toString(e.getLineNumber()));
        }

        this.getResult().setOutput(this.errors);
   }

   protected CSARArchive createArchiveInstance(){
        return new CSARArchive();
   }
}
