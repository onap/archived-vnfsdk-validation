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

package org.onap.validation.csar;

import java.io.IOException;

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates CSAR
 */
@OnapCommandSchema(schema = "vtp-validate-csar-casablanca.yaml")
public class VTPValidateCSAR extends OnapCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSAR.class);

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String csar = (String) getParametersMap().get("csar").getValue();

        //run the test cases
        String error = this.test(csar);

        //set the result
        this.getResult().getRecordsMap().get("errors").getValues().add(error);
   }

   public String test(String csar) throws OnapCommandException {
       try {
           CsarValidator cv = new CsarValidator(csar, csar);
           return cv.validateCsar();
       } catch(IOException e) {
           throw new OnapCommandExecutionFailed(e.getMessage());
       }
   }
}
