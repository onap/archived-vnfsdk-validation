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
import org.onap.cvc.csar.security.CmsSignatureValidator;
import org.onap.cvc.csar.security.CmsSignatureValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@OnapCommandSchema(schema = "vtp-validate-csar-r787965.yaml")
public class VTPValidateCSARR787965 extends VTPValidateCSARBase {

    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR787965.class);

    static class CSARErrorInvalidSignature extends CSARArchive.CSARError {
        CSARErrorInvalidSignature() {
            super("0x3001");
            this.message = "Invalid CSAR signature!";
        }
    }

    static class CsarFileNotAvailableError extends CSARArchive.CSARError {
        CsarFileNotAvailableError() {
            super("0x3002");
            this.message = "Missing. Csar file is not available!";
        }
    }

    static class SignatureWithCertificationOnlyWarning extends CSARArchive.CSARError {
        SignatureWithCertificationOnlyWarning() {
            super("0x3003");
            this.message = "Warning. Zip package probably is valid. " +
                    "It contains only signature with certification cms and csar package. " +
                    "Unable to verify csar signature.";
        }
    }


    static class BrokenZipPackageError extends CSARArchive.CSARError {
        BrokenZipPackageError() {
            super("0x3004");
            this.message = "Missing. Unable to find certification files.";
        }
    }


    @Override
    protected void validateCSAR(CSARArchive csar) throws OnapCommandException {

        try {
            FileArchive.Workspace workspace = csar.getWorkspace();
            if (workspace.isZip()) {
                verifyZipStructure(workspace);
            }
        } catch (Exception e) {
            LOG.error("Internal VTPValidateCSARR787965 command error", e);
            throw new OnapCommandException("0x3000", "Internal VTPValidateCSARR787965 command error. See logs.");
        }

    }

    private void verifyZipStructure(FileArchive.Workspace workspace) throws IOException, CmsSignatureValidatorException {
        final Optional<Path> pathToCsarFile = workspace.getPathToCsarFile();
        final Optional<Path> pathToCertFile = workspace.getPathToCertFile();
        final Optional<Path> pathToCmsFile = workspace.getPathToCmsFile();
        if(!pathToCsarFile.isPresent()) {
            this.errors.add(new CsarFileNotAvailableError());
        } else {
            if (pathToCertFile.isPresent() && pathToCmsFile.isPresent()) {
                verifyTwoFileCertification(pathToCsarFile.get(), pathToCertFile.get(), pathToCmsFile.get());
            } else if (pathToCmsFile.isPresent()) {
                this.errors.add(new SignatureWithCertificationOnlyWarning());
            } else {
                this.errors.add(new BrokenZipPackageError());
            }
        }
    }

    private void verifyTwoFileCertification(Path pathToCsarFile, Path pathToCertFile, Path pathToCmsFile) throws IOException, CmsSignatureValidatorException {
        final CmsSignatureValidator securityManager = new CmsSignatureValidator();

        byte[] csarContent = Files.readAllBytes(pathToCsarFile);
        byte[] signature = Files.readAllBytes(pathToCmsFile);
        byte[] publicCertification = Files.readAllBytes(pathToCertFile);

        if (!securityManager.verifySignedData(signature, publicCertification,csarContent)) {
            this.errors.add(new CSARErrorInvalidSignature());
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R787965";
    }


}
