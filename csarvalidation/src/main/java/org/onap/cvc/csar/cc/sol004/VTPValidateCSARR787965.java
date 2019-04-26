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
import org.onap.cvc.csar.FileArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.rsa.RSACertificateValidator;
import org.onap.cvc.csar.rsa.X509RsaCertification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

@OnapCommandSchema(schema = "vtp-validate-csar-r787965.yaml")
public class VTPValidateCSARR787965 extends VTPValidateCSARBase {

    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR787965.class);

    public static class CSARErrorInvalidSignature extends CSARArchive.CSARError {
        CSARErrorInvalidSignature() {
            super("0x3001");
            this.message = "Invalid CSAR signature!";
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws OnapCommandException {

        try {
            final RSACertificateValidator rsaCertificateValidator = new RSACertificateValidator(new X509RsaCertification());

            FileArchive.Workspace workspace = csar.getWorkspace();
            final Optional<Path> pathToCsarFile = workspace.getPathToCsarFile();
            final Optional<Path> pathToCertFile = workspace.getPathToCertFile();
            final Optional<Path> pathToCmsFile = workspace.getPathToCmsFile();

            if (workspace.isZip() && pathToCsarFile.isPresent() && pathToCertFile.isPresent() && pathToCmsFile.isPresent()) {
                    byte[] csarContent = Files.readAllBytes(pathToCsarFile.get());
                    String signature = Base64.getEncoder().encodeToString(Files.readAllBytes(pathToCmsFile.get()));
                    String publicCertification = Base64.getEncoder().encodeToString(Files.readAllBytes(pathToCertFile.get()));

                    if (!rsaCertificateValidator.isValid(csarContent, signature, publicCertification)) {
                        this.errors.add(new CSARErrorInvalidSignature());
                    }
                }

        } catch (Exception e) {
            LOG.error("Internal VTPValidateCSARR787965 command error", e);
            throw new OnapCommandException("0x3000", "Internal VTPValidateCSARR787965 command error. See logs.");
        }

    }

    @Override
    protected String getVnfReqsNo() {
        return "R787965";
    }


}
