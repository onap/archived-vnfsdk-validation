/*
  Copyright 2020 Nokia
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.parser.SourcesParser;
import org.onap.cvc.csar.security.CmsSignatureData;
import org.onap.cvc.csar.security.CmsSignatureLoadingException;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.onap.validation.csar.FileUtil.fileHaveOneOfExtensions;
import static org.onap.validation.csar.FileUtil.filesAreInSameDirectory;
import static org.onap.validation.csar.FileUtil.filesHaveSameNamesIgnoringExtensions;

public class CsarSourceSecurityValidator {

    public static final String SIGNATURE_FILE_TYPE = "signature";
    private static final String[] ARTIFACT_SIGNATURE_EXTENSIONS = {".sig.cms"};
    public static final String CERTIFICATE_FILE_TYPE = "certificate";
    private static final String[] ARTIFACT_CERTIFICATE_EXTENSIONS = {".cert"};

    private final FileSignatureValidator fileSignatureValidator = new FileSignatureValidator();
    private final FileHashValidator fileHashValidator = new FileHashValidator();

    private final SourcesParser.Source source;
    private final Path csarRootDirectory;
    private Path commonCertificate = null;

    private List<CSARArchive.CSARError> errors;

    public CsarSourceSecurityValidator(SourcesParser.Source source, Path csarRootDirectory) {
        this.source = source;
        this.csarRootDirectory = csarRootDirectory;
    }

    public List<CSARArchive.CSARError>  validate() throws NoSuchAlgorithmException, IOException {
        errors = new ArrayList<>();
        validateSource();
        return errors;
    }

    public List<CSARArchive.CSARError>  validate(Path commonCertificate) throws NoSuchAlgorithmException, IOException {
        this.commonCertificate = commonCertificate;
        return validate();
    }

    private void validateSource()
        throws NoSuchAlgorithmException, IOException {
        final Path sourcePath = csarRootDirectory.resolve(source.getValue());
        if (sourcePath.toFile().exists()) {
            validateHashIfPresent();
            validateArtifactSignatureIfPresent();
        } else {
            this.errors.add(new Error.CSARErrorUnableToFindSource(source.getValue()));
        }
    }

    private void validateHashIfPresent()
        throws NoSuchAlgorithmException, IOException {
        if (!source.getAlgorithm().isEmpty()) {
            validateSourceHashCode();
        } else if (source.getAlgorithm().isEmpty() && !source.getHash().isEmpty()) {
            this.errors.add(new Error.CSARErrorUnableToFindAlgorithm(source.getValue()));
        }
    }

    private void validateSourceHashCode()
        throws NoSuchAlgorithmException, IOException {
        if (!fileHashValidator.isValid(
            source.getHash(),csarRootDirectory.resolve(source.getValue()),source.getAlgorithm())
        ) {
            this.errors.add(new Error.CSARErrorWrongHashCode(source.getValue()));
        }
    }

    private void validateArtifactSignatureIfPresent()
        throws IOException {
        boolean containsSignatureTag = !source.getSignature().isEmpty();
        Optional<Path> signature = Optional.empty();
        boolean containsCertificateTag = !source.getCertificate().isEmpty();
        Optional<Path> certificate;

        if (containsSignatureTag) {
            signature = validateSignatureConstrains(source.getValue(), source.getSignature());
        }
        if (containsCertificateTag) {
            certificate = validateCertificateConstrains(source.getValue(), source.getCertificate());
        } else {
            certificate = Optional.ofNullable(this.commonCertificate);
        }

        if (containsSignatureTag) {
            if (signature.isPresent() && certificate.isPresent()) {
                validateArtifactSignature(
                    csarRootDirectory.resolve(source.getValue()), signature.get(), certificate.get()
                );
            } else if (certificate.isEmpty() && !containsCertificateTag) {
                this.errors.add(new Error.CSARErrorUnableToFindArtifactCertificateTag(source.getValue()));
            }
        } else if (containsCertificateTag) {
            this.errors.add(new Error.CSARErrorUnableToFindArtifactSignatureTag(source.getValue()));
        }
    }

    private Optional<Path> validateSignatureConstrains(String pathToSourceFile, String pathToSignatureFile) {
        if (
            validateSecurityFileConstrains(
                pathToSourceFile, pathToSignatureFile, ARTIFACT_SIGNATURE_EXTENSIONS, SIGNATURE_FILE_TYPE
            )
        ) {
            return Optional.of(csarRootDirectory.resolve(pathToSignatureFile));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Path> validateCertificateConstrains(String pathToSourceFile, String pathToCertFile) {
        if (
            validateSecurityFileConstrains(
                pathToSourceFile, pathToCertFile, ARTIFACT_CERTIFICATE_EXTENSIONS, CERTIFICATE_FILE_TYPE
            )
        ) {
            return Optional.of(csarRootDirectory.resolve(pathToCertFile));
        } else {
            return Optional.empty();
        }
    }

    private boolean validateSecurityFileConstrains(
        String pathToSourceFile, String pathToSecurityFile,
        String[] securityFileExtensions, String securityFileType
    ) {
        boolean areSignatureConstrainsValid = false;
        final Path filePath = csarRootDirectory.resolve(pathToSourceFile);
        final Path securityFilePath = csarRootDirectory.resolve(pathToSecurityFile);
        if (!filesAreInSameDirectory(filePath, securityFilePath)) {
            this.errors.add(
                new Error.CSARErrorWrongSecurityFileLocation(pathToSourceFile, pathToSecurityFile, securityFileType)
            );
        } else if (!filesHaveSameNamesIgnoringExtensions(filePath, securityFilePath)) {
            this.errors.add(
                new Error.CSARErrorWrongSecurityFileName(pathToSourceFile, securityFilePath.getFileName().toString(), securityFileType)
            );
        } else if (!fileHaveOneOfExtensions(securityFilePath, securityFileExtensions)) {
            this.errors.add(
                new Error.CSARErrorWrongSecurityFileExtension(pathToSourceFile, securityFileExtensions, securityFileType)
            );
        } else if (!securityFilePath.toFile().exists()) {
            this.errors.add(
                new Error.CSARErrorUnableToFindArtifactSecurityFile(pathToSourceFile, pathToSecurityFile, securityFileType)
            );
        } else {
            areSignatureConstrainsValid = true;
        }
        return areSignatureConstrainsValid;
    }


    private void validateArtifactSignature(Path filePath, Path signaturePath, Path certificatePath)
        throws IOException {
        try {
            CmsSignatureData signatureData =
                fileSignatureValidator.createSignatureData(filePath, signaturePath, certificatePath);
            if (!fileSignatureValidator.isValid(signatureData)) {
                this.errors.add(new Error.CSARErrorIncorrectArtifactSignature(source.getValue()));
            }
        } catch (CmsSignatureLoadingException e) {
            this.errors.add(new Error.CSARErrorFailToLoadArtifactSignature(source.getValue(), source.getSignature()));
        }
    }

}
