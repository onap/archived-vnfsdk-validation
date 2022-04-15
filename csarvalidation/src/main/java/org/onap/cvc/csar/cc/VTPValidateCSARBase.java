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

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.PnfCSARArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class VTPValidateCSARBase extends OnapCommand {
    protected static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARBase.class);

    protected List<CSARError> errors = new ArrayList<>();

    protected abstract void validateCSAR(CSARArchive csar) throws Exception; //NOSONAR

    protected abstract String getVnfReqsNo();



    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();
        boolean isPnf = isPnf();

        //execute
        try (CSARArchive csar = isPnf ? new PnfCSARArchive(): new CSARArchive()){

            csar.init(path);
            csar.parse();

            errors.addAll(csar.getErrors());

            this.validateCSAR(csar);

        } catch (Exception e) {
            String errorMsg = this.getVnfReqsNo() + ": Failed to validate CSAR " + e;
            throw new OnapCommandExecutionFailed(errorMsg);
        }

        for (CSARError e: this.errors) {
            e.setVnfreqNo(this.getVnfReqsNo());
            this.getResult().getRecordsMap().get("code").getValues().add(this.getVnfReqsNo() + "-" + e.getCode());
            this.getResult().getRecordsMap().get("message").getValues().add(e.getMessage());
            this.getResult().getRecordsMap().get("file").getValues().add(e.getFile());
            this.getResult().getRecordsMap().get("line-no").getValues().add(Integer.toString(e.getLineNumber()));
        }

        this.getResult().setOutput(this.errors);
        this.getResult().setPassed(this.errors.isEmpty());
   }

    private boolean isPnf() {
        final OnapCommandParameter pnf = getParametersMap().get("pnf");
        return pnf != null && (boolean) pnf.getValue();
    }

    public List<CSARError> getErrors() {
        return this.errors;
    }
}
