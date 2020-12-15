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


import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.cc.sol004.r130206.CsarSecurityValidator;
import org.onap.cvc.csar.cc.sol004.r130206.Error;

import java.nio.file.Path;
import java.util.Optional;

@OnapCommandSchema(schema = "vtp-validate-csar-r130206.yaml")
public class VTPValidateCSARR130206 extends VTPValidateCSARBase {


    @Override
    protected void validateCSAR(CSARArchive csar) throws OnapCommandException {

        try {
            final Optional<Path> pathToCsarFolder = getPathToCsar(csar);
            if (pathToCsarFolder.isPresent()) {
                final CsarSecurityValidator csarSecurityValidator = new CsarSecurityValidator(csar, pathToCsarFolder.get());
                this.errors.addAll(csarSecurityValidator.validate());
            } else {
                this.errors.add(new Error.CSARErrorUnableToFindCsarContent());
            }
        } catch (Exception e) {
            LOG.error("Internal VTPValidateCSARR130206 command error", e);
            throw new OnapCommandException("0x3000", "Internal VTPValidateCSARR130206 command error. See logs.");
        }

    }

    private Optional<Path> getPathToCsar(CSARArchive csar) {
        return csar.getWorkspace().getPathToCsarFolder();
    }

    @Override
    protected String getVnfReqsNo() {
        return "R130206";
    }


}
