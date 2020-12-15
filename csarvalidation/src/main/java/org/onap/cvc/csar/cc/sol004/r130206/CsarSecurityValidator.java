/**
 * Copyright 2020 Nokia
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
 */

package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.parser.SourcesParser;
import org.onap.cvc.csar.security.CertificateLoadingException;
import org.onap.cvc.csar.security.CmsSignatureData;
import org.onap.cvc.csar.security.CmsSignatureLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsarSecurityValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CsarSecurityValidator.class);

    private static final String EMPTY_STRING = "";
    private final FileSignatureValidator fileSignatureValidator = new FileSignatureValidator();

    private List<CSARArchive.CSARError> errors;

    private final CSARArchive csar;
    private final Path csarRootDirectory;

    public CsarSecurityValidator(CSARArchive csar, Path csarRootDirectory) {
        this.csar = csar;
        this.csarRootDirectory = csarRootDirectory;
    }

    public List<CSARArchive.CSARError> validate() throws IOException, NoSuchAlgorithmException {
        errors = new ArrayList<>();
        if (containsCms()) {
            validateCsarSecurity();
        } else if (containAnySecurityElements()) {
            this.errors.add(new Error.CSARErrorUnableToFindCms());
        } else {
            this.errors.add(new Error.CSARWarningNoSecurity());
        }
        return errors;
    }

    private boolean containAnySecurityElements() {
        return (containsToscaMeta() && containsCertificateInTosca()) ||
            containsCertificateInRootCatalog() ||
            containsPerArtifactSecurity();
    }

    private void validateCsarSecurity() throws NoSuchAlgorithmException, IOException {
        try {
            CmsSignatureData signatureData = this.fileSignatureValidator.createSignatureDataForManifestFile(csar.getManifestMfFile());
            if (signatureData.getCertificate().isPresent()) {
                validateCertificationUsingCmsCertificate(signatureData);
            } else if (containsToscaMeta()) {
                validateCertificationUsingTosca(signatureData);
            } else if (containsCertificateInRootCatalog()) {
                validateCertificationUsingCertificateFromRootDirectory(signatureData);
            } else {
                this.errors.add(new Error.CSARErrorUnableToFindCertificate());
            }
        } catch (CmsSignatureLoadingException e) {
            LOG.error("Unable to load CMS!", e);
            this.errors.add(new Error.CSARErrorUnableToLoadCms());
        }
    }

    private boolean containsCms() {
        String cms = csar.getManifest().getCms();
        return cms != null && !cms.equals(EMPTY_STRING);
    }

    private boolean containsToscaMeta() {
        return csar.getToscaMetaFile() != null;
    }

    private boolean containsCertificateInTosca() {
        String certificate = csar.getToscaMeta().getEntryCertificate();
        return certificate != null && !certificate.equals(EMPTY_STRING);
    }

    private boolean containsCertificateInRootCatalog() {
        File potentialCertificateFileInRootDirectory = getCertificateFromRootDirectory();
        return potentialCertificateFileInRootDirectory.exists();
    }

    private boolean containsPerArtifactSecurity() {
        return csar.getManifest().getSources().stream().anyMatch(
            source ->
                !source.getAlgorithm().equals(EMPTY_STRING) ||
                    !source.getHash().equals(EMPTY_STRING) ||
                    !source.getCertificate().equals(EMPTY_STRING) ||
                    !source.getSignature().equals(EMPTY_STRING)
        );
    }

    private void validateCertificationUsingCmsCertificate(CmsSignatureData signatureData)
        throws NoSuchAlgorithmException, IOException {
        validateAllSources();
        validateFileSignature(signatureData);
        if (containsCertificateInTosca()) {
            errors.add(new Error.CSARErrorEntryCertificateIsDefinedDespiteTheCms());
            if (csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).exists()) {
                errors.add(new Error.CSARErrorEntryCertificateIsPresentDespiteTheCms());
            }
        }
        if (containsCertificateInRootCatalog()) {
            errors.add(new Error.CSARErrorRootCertificateIsPresentDespiteTheCms());
        }
    }

    private void validateCertificationUsingTosca(CmsSignatureData signatureData)
        throws NoSuchAlgorithmException, IOException {
        Optional<Path> pathToCert = loadCertificateFromTosca();
        if (pathToCert.isPresent()) {
            validateAllSources(pathToCert.get());
            signatureData.loadCertificate(pathToCert.get());
            validateFileSignature(signatureData);
        }
        if (containsCertificateInRootCatalog() && rootCertificateIsNotReferredAsToscaEtsiEntryCertificate()) {
            errors.add(new Error.CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate());
        }
    }

    private Optional<Path> loadCertificateFromTosca() {
        if (csar.getToscaMeta().getEntryCertificate() != null) {
            final Path absolutePathToEntryCertificate = csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).toPath();
            if (absolutePathToEntryCertificate.toFile().exists()) {
                return Optional.of(absolutePathToEntryCertificate);
            } else {
                this.errors.add(new Error.CSARErrorUnableToFindEntryCertificate());
                return Optional.empty();
            }
        } else {
            this.errors.add(new Error.CSARErrorUnableToFindCertificateEntryInTosca());
            return Optional.empty();
        }
    }

    private boolean rootCertificateIsNotReferredAsToscaEtsiEntryCertificate() {
        String pathToRootCertificate = getCertificateFromRootDirectory().getPath();
        String pathToEntryEtsiCertificate = csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).getPath();
        return !pathToRootCertificate.equals(pathToEntryEtsiCertificate);
    }

    private void validateCertificationUsingCertificateFromRootDirectory(CmsSignatureData signatureData)
        throws NoSuchAlgorithmException, IOException {
        Optional<Path> pathToCert = loadCertificateFromRootDirectory();
        if (pathToCert.isPresent()) {
            validateAllSources(pathToCert.get());
            signatureData.loadCertificate(pathToCert.get());
            validateFileSignature(signatureData);
        }
    }

    private void validateFileSignature(CmsSignatureData signatureData) {
        final boolean isValid = this.fileSignatureValidator.isValid(signatureData);
        if (!isValid) {
            this.errors.add(new Error.CSARErrorInvalidSignature());
        }
    }

    private Optional<Path> loadCertificateFromRootDirectory() {
        try {
            Path pathToCertificateInRootDirectory = getCertificateFromRootDirectory().toPath();
            return Optional.of(pathToCertificateInRootDirectory);
        } catch (CertificateLoadingException e) {
            LOG.error("Unable to read ETSI entry certificate file!", e);
            return Optional.empty();
        }
    }

    private File getCertificateFromRootDirectory() {
        String nameOfCertificate =
            csar.getManifestMfFile().getName().split("\\.")[0] +
                ".cert";
        return csar.getFileFromCsar(nameOfCertificate);
    }

    private void validateAllSources()
        throws NoSuchAlgorithmException, IOException {
        errors.addAll(createCsarSourcesValidator().validate());
    }

    private void validateAllSources(Path commonCertificate)
        throws NoSuchAlgorithmException, IOException {
        errors.addAll(createCsarSourcesValidator().validate(commonCertificate));
    }

    private CsarSourcesSecurityValidator createCsarSourcesValidator() {
        final CSARArchive.Manifest manifest = csar.getManifest();
        final Map<String, Map<String, List<String>>> nonMano = manifest.getNonMano();
        final List<SourcesParser.Source> sources = manifest.getSources();
        return new CsarSourcesSecurityValidator(nonMano, sources, csarRootDirectory);
    }

}
