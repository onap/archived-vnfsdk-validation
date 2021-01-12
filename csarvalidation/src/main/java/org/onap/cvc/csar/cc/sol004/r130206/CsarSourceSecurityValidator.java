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
import org.onap.cvc.csar.cc.sol004.r130206.artifact.ArtifactSecurityFileValidator;
import org.onap.cvc.csar.cc.sol004.r130206.artifact.ArtifactSecurityFileValidatorFactory;
import org.onap.cvc.csar.cc.sol004.r130206.artifact.ValidatedSecurityFile;
import org.onap.cvc.csar.parser.SourcesParser;
import org.onap.cvc.csar.security.CmsSignatureData;
import org.onap.cvc.csar.security.CmsSignatureLoadingException;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CsarSourceSecurityValidator {

    public static final String SIGNATURE_FILE_TYPE = "signature";
    private static final String[] ARTIFACT_SIGNATURE_EXTENSIONS = {".sig.cms",".sig.p7b",".sig.p7c"};
    public static final String CERTIFICATE_FILE_TYPE = "certificate";
    private static final String[] ARTIFACT_CERTIFICATE_EXTENSIONS = {".cert"};

    private final FileSignatureValidator fileSignatureValidator = new FileSignatureValidator();
    private final FileHashValidator fileHashValidator = new FileHashValidator();

    private final SourcesParser.Source source;
    private final Path csarRootDirectory;

    private final ArtifactSecurityFileValidator signatureValidator;
    private final ArtifactSecurityFileValidator certificateValidator;

    public CsarSourceSecurityValidator(SourcesParser.Source source, Path csarRootDirectory) {
        this.source = source;
        this.csarRootDirectory = csarRootDirectory;
        ArtifactSecurityFileValidatorFactory fileValidatorFactory =
            new ArtifactSecurityFileValidatorFactory(csarRootDirectory, source.getValue());
        signatureValidator = fileValidatorFactory.create(source.getSignature());
        certificateValidator = fileValidatorFactory.create(source.getCertificate());
    }

    public CsarSourceSecurityValidator(SourcesParser.Source source, Path csarRootDirectory, Path commonCert) {
        this.source = source;
        this.csarRootDirectory = csarRootDirectory;
        ArtifactSecurityFileValidatorFactory fileValidatorFactory =
            new ArtifactSecurityFileValidatorFactory(csarRootDirectory, source.getValue());
        signatureValidator = fileValidatorFactory.create(source.getSignature());
        certificateValidator = fileValidatorFactory.create(source.getCertificate(), commonCert);
    }

    public List<CSARArchive.CSARError> validate() throws NoSuchAlgorithmException, IOException {
        return validateSource();
    }

    private List<CSARArchive.CSARError> validateSource()
        throws NoSuchAlgorithmException, IOException {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        final Path sourcePath = csarRootDirectory.resolve(source.getValue());
        if (sourcePath.toFile().exists()) {
            errors.addAll(validateHashIfPresent());
            errors.addAll(validateArtifactSignatureIfPresent());
        } else {
            errors.add(new Error.CSARErrorUnableToFindSource(source.getValue()));
        }
        return errors;
    }

    private List<CSARArchive.CSARError> validateHashIfPresent()
        throws NoSuchAlgorithmException, IOException {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        if (!source.getAlgorithm().isEmpty()) {
            errors.addAll(validateSourceHashCode());
        } else if (source.getAlgorithm().isEmpty() && !source.getHash().isEmpty()) {
            errors.add(new Error.CSARErrorUnableToFindAlgorithm(source.getValue()));
        }
        return errors;
    }

    private List<CSARArchive.CSARError> validateSourceHashCode()
        throws NoSuchAlgorithmException, IOException {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        if (!fileHashValidator.isValid(
            source.getHash(), csarRootDirectory.resolve(source.getValue()), source.getAlgorithm())
        ) {
            errors.add(new Error.CSARErrorWrongHashCode(source.getValue()));
        }
        return errors;
    }

    private List<CSARArchive.CSARError> validateArtifactSignatureIfPresent()
        throws IOException {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        final boolean containsSignatureTag = !source.getSignature().isEmpty();
        final boolean containsCertificateTag = !source.getCertificate().isEmpty();

        ValidatedSecurityFile validatedSignature = signatureValidator.getValidatedSecurityFile(
            source.getValue(), source.getSignature(), ARTIFACT_SIGNATURE_EXTENSIONS, SIGNATURE_FILE_TYPE
        );
        errors.addAll(validatedSignature.getErrors());
        ValidatedSecurityFile validatedCertificate = certificateValidator.getValidatedSecurityFile(
            source.getValue(), source.getCertificate(), ARTIFACT_CERTIFICATE_EXTENSIONS, CERTIFICATE_FILE_TYPE
        );
        errors.addAll(validatedCertificate.getErrors());

        if (containsSignatureTag) {
            if (validatedCertificate.isValid() && validatedSignature.isValid()) {
                errors.addAll(
                    validateArtifactSignature(
                        csarRootDirectory.resolve(source.getValue()),
                        validatedSignature.getFilePath(), validatedCertificate.getFilePath()
                    )
                );
            } else if (!validatedCertificate.isValid() && !containsCertificateTag) {
                errors.add(new Error.CSARErrorUnableToFindArtifactCertificateTag(source.getValue()));
            }

        } else if (containsCertificateTag) {
            errors.add(new Error.CSARErrorUnableToFindArtifactSignatureTag(source.getValue()));
        }
        return errors;
    }

    private List<CSARArchive.CSARError> validateArtifactSignature(Path filePath, Path signaturePath, Path certificatePath)
        throws IOException {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        try {
            final CmsSignatureData signatureData =
                fileSignatureValidator.createSignatureData(filePath, signaturePath, certificatePath);
            if (!fileSignatureValidator.isValid(signatureData)) {
                errors.add(new Error.CSARErrorIncorrectArtifactSignature(source.getValue()));
            }
        } catch (CmsSignatureLoadingException e) {
            errors.add(new Error.CSARErrorFailToLoadArtifactSignature(source.getValue(), source.getSignature()));
        }
        return errors;
    }

}
