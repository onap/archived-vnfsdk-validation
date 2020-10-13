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
import org.onap.cvc.csar.parser.ManifestFileModel;
import org.onap.cvc.csar.parser.ManifestFileSplitter;
import org.onap.cvc.csar.parser.SourcesParser;
import org.onap.cvc.csar.security.CertificateLoadingException;
import org.onap.cvc.csar.security.CmsSignatureData;
import org.onap.cvc.csar.security.CmsSignatureDataFactory;
import org.onap.cvc.csar.security.CmsSignatureLoadingException;
import org.onap.cvc.csar.security.CmsSignatureValidator;
import org.onap.cvc.csar.security.CmsSignatureValidatorException;
import org.onap.cvc.csar.security.ShaHashCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@OnapCommandSchema(schema = "vtp-validate-csar-r130206.yaml")
public class VTPValidateCSARR130206 extends VTPValidateCSARBase {

    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR130206.class);
    private static final String SHA_256 = "SHA-256";
    private static final String SHA_512 = "SHA-512";
    private static final String EMPTY_STRING = "";

    private final ShaHashCodeGenerator shaHashCodeGenerator = new ShaHashCodeGenerator();
    private final ManifestFileSignatureValidator manifestFileSignatureValidator = new ManifestFileSignatureValidator();

    public static class CSARErrorUnableToFindCertificate extends CSARArchive.CSARError {

        CSARErrorUnableToFindCertificate() {
            super("0x4001");
            this.message = "Unable to find cert file!";
        }
    }

    public static class CSARErrorUnableToFindCms extends CSARArchive.CSARError {

        CSARErrorUnableToFindCms() {
            super("0x4002");
            this.message = "Unable to find cms signature!";
        }
    }

    public static class CSARErrorUnableToLoadCms extends CSARArchive.CSARError {
        CSARErrorUnableToLoadCms() {
            super("0x4002");
            this.message = "Unable to load cms signature!";
        }
    }

    public static class CSARErrorUnableToFindCsarContent extends CSARArchive.CSARError {

        CSARErrorUnableToFindCsarContent() {
            super("0x4003");
            this.message = "Unable to find csar content!";
        }
    }

    public static class CSARErrorWrongHashCode extends CSARArchive.CSARError {

        CSARErrorWrongHashCode(String path) {
            super("0x4004");
            this.message = String.format("Source '%s' has wrong hash!", path);
        }
    }

    public static class CSARErrorUnableToFindAlgorithm extends CSARArchive.CSARError {

        CSARErrorUnableToFindAlgorithm(String path) {
            super("0x4005");
            this.message = String.format("Source '%s' has hash, but unable to find algorithm tag!", path);
        }
    }

    public static class CSARErrorUnableToFindSource extends CSARArchive.CSARError {

        CSARErrorUnableToFindSource(String path) {
            super("0x4006");
            this.message = String.format("Unable to calculate digest - file missing: %s", path);
        }
    }

    public static class CSARErrorInvalidSignature extends CSARArchive.CSARError {

        CSARErrorInvalidSignature() {
            super("0x4007");
            this.message = "File has invalid signature!";
        }
    }

    public static class CSARErrorContentMismatch extends CSARArchive.CSARError {

        CSARErrorContentMismatch() {
            super("0x4008");
            this.message = "Mismatch between contents of non-mano-artifact-sets and source files of the package";
        }
    }

    public static class CSARErrorUnableToFindEntryCertificate extends CSARArchive.CSARError {

        CSARErrorUnableToFindEntryCertificate() {
            super("0x4009");
            this.message = "Unable to find cert file defined by ETSI-Entry-Certificate!";
        }
    }

    public static class CSARErrorEntryCertificateIsDefinedDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorEntryCertificateIsDefinedDespiteTheCms() {
            super("0x4011");
            this.message = "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorEntryCertificateIsPresentDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorEntryCertificateIsPresentDespiteTheCms() {
            super("0x4012");
            this.message = "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorRootCertificateIsPresentDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorRootCertificateIsPresentDespiteTheCms() {
            super("0x4013");
            this.message = "Certificate present in root catalog despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate extends CSARArchive.CSARError {

        CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate() {
            super("0x4013");
            this.message = "Certificate present in root catalog despite the TOSCA.meta file";
        }
    }

    public static class CSARErrorUnableToFindCertificateEntryInTosca extends CSARArchive.CSARError {

        CSARErrorUnableToFindCertificateEntryInTosca() {
            super("0x4014");
            this.message = "Unable to find ETSI-Entry-Certificate in Tosca file";
        }
    }

    public static class CSARWarningNoSecurity extends CSARArchive.CSARErrorWarning {
        CSARWarningNoSecurity() {
            super(EMPTY_STRING, EMPTY_STRING, -1, EMPTY_STRING);
            this.message = "Warning. Consider adding package integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1";
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws OnapCommandException {

        try {
            FileArchive.Workspace workspace = csar.getWorkspace();
            final Optional<Path> pathToCsarFolder = workspace.getPathToCsarFolder();
            if (pathToCsarFolder.isPresent()) {
                validate(csar, pathToCsarFolder.get());
            } else {
                this.errors.add(new CSARErrorUnableToFindCsarContent());
            }
        } catch (Exception e) {
            LOG.error("Internal VTPValidateCSARR130206 command error", e);
            throw new OnapCommandException("0x3000", "Internal VTPValidateCSARR130206 command error. See logs.");
        }

    }

    private void validate(CSARArchive csar, Path csarRootDirectory) throws IOException, NoSuchAlgorithmException {
        if (containsCms(csar.getManifest())) {
            validateCmsSignature(csar, csarRootDirectory);
        } else if (
            ( containsToscaMeta(csar) && containsCertificateInTosca(csar.getToscaMeta()) ) ||
                containsCertificateInRootCatalog(csar) ||
                containsHashOrAlgorithm(csar.getManifest())) {
            this.errors.add(new CSARErrorUnableToFindCms());
        } else {
            this.errors.add(new CSARWarningNoSecurity());
        }
    }

    private void validateCmsSignature(CSARArchive csar, Path csarRootDirectory) throws NoSuchAlgorithmException, IOException {
        try {
            CmsSignatureData signatureData = this.manifestFileSignatureValidator.createSignatureData(csar.getManifestMfFile());
            if (signatureData.getCertificate().isPresent()) {
                validateCertificationUsingCmsCertificate(signatureData, csar, csarRootDirectory);
            } else if (containsToscaMeta(csar)) {
                validateCertificationUsingTosca(signatureData, csar, csarRootDirectory);
            } else if (containsCertificateInRootCatalog(csar)) {
                validateCertificationUsingCertificateFromRootDirectory(signatureData, csar, csarRootDirectory);
            } else {
                this.errors.add(new CSARErrorUnableToFindCertificate());
            }
        } catch (CmsSignatureLoadingException e) {
            LOG.error("Unable to load CMS!", e);
            this.errors.add(new CSARErrorUnableToLoadCms());
        }
    }

    private boolean containsCms(CSARArchive.Manifest manifest) {
        String cms = manifest.getCms();
        return cms != null && !cms.equals(EMPTY_STRING);
    }

    private boolean containsToscaMeta(CSARArchive archive) {
        return archive.getToscaMetaFile() != null;
    }

    private boolean containsCertificateInTosca(CSARArchive.TOSCAMeta toscaMeta) {
        String certificate = toscaMeta.getEntryCertificate();
        return certificate != null && !certificate.equals(EMPTY_STRING);
    }

    private boolean containsCertificateInRootCatalog(CSARArchive csar) {
        File potentialCertificateFileInRootDirectory = getCertificateFromRootDirectory(csar);
        return potentialCertificateFileInRootDirectory.exists();
    }

    private boolean containsHashOrAlgorithm(CSARArchive.Manifest manifest) {
        return manifest.getSources().stream().anyMatch(
            source ->
                !source.getAlgorithm().equals(EMPTY_STRING) ||
                    !source.getHash().equals(EMPTY_STRING)
        );
    }

    private void validateCertificationUsingCmsCertificate(CmsSignatureData signatureData, CSARArchive csar, Path csarRootDirectory)
        throws NoSuchAlgorithmException, IOException {
        validateAllSources(csar, csarRootDirectory);
        validateFileSignature(signatureData);
        if (containsCertificateInTosca(csar.getToscaMeta())) {
            errors.add(new CSARErrorEntryCertificateIsDefinedDespiteTheCms());
            if (csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).exists()) {
                errors.add(new CSARErrorEntryCertificateIsPresentDespiteTheCms());
            }
        }
        if (containsCertificateInRootCatalog(csar)) {
            errors.add(new CSARErrorRootCertificateIsPresentDespiteTheCms());
        }
    }

    private void validateCertificationUsingTosca(CmsSignatureData signatureData, CSARArchive csar, Path csarRootDirectory)
        throws NoSuchAlgorithmException, IOException {
        validateAllSources(csar, csarRootDirectory);
        if (loadCertificateFromTosca(signatureData, csar)) {
            validateFileSignature(signatureData);
        }
        if (containsCertificateInRootCatalog(csar) && rootCertificateIsNotReferredAsToscaEtsiEntryCertificate(csar)) {
            errors.add(new CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate());
        }
    }

    private boolean loadCertificateFromTosca(CmsSignatureData signatureData, CSARArchive csar) {
        if(csar.getToscaMeta().getEntryCertificate() != null) {
            try {
                final Path absolutePathToEntryCertificate = csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).toPath();
                signatureData.loadCertificate(absolutePathToEntryCertificate);
                return true;
            } catch (CertificateLoadingException e) {
                this.errors.add(new CSARErrorUnableToFindEntryCertificate());
                return false;
            }
        } else {
            this.errors.add(new CSARErrorUnableToFindCertificateEntryInTosca());
            return false;
        }
    }

    private boolean rootCertificateIsNotReferredAsToscaEtsiEntryCertificate(CSARArchive csar) {
        String pathToRootCertificate = getCertificateFromRootDirectory(csar).getPath();
        String pathToEntryEtsiCertificate = csar.getFileFromCsar(csar.getToscaMeta().getEntryCertificate()).getPath();
        return !pathToRootCertificate.equals(pathToEntryEtsiCertificate);
    }

    private void validateCertificationUsingCertificateFromRootDirectory(CmsSignatureData signatureData, CSARArchive csar, Path csarRootDirectory)
        throws NoSuchAlgorithmException, IOException {
        validateAllSources(csar, csarRootDirectory);
        if (loadCertificateFromRootDirectory(signatureData, csar)) {
            validateFileSignature(signatureData);
        }
    }

    private boolean loadCertificateFromRootDirectory(CmsSignatureData signatureData, CSARArchive csar) {
        try {
            File certificateFileFromRootDirectory = getCertificateFromRootDirectory(csar);
            signatureData.loadCertificate(certificateFileFromRootDirectory.toPath());
            return true;
        } catch (CertificateLoadingException e) {
            LOG.error("Uable to read ETSI entry certificate file!", e);
            return false;
        }
    }

    private File getCertificateFromRootDirectory(CSARArchive csar) {
        String nameOfCertificate =
            csar.getManifestMfFile().getName().split("\\.")[0] +
                ".cert";
        return csar.getFileFromCsar(nameOfCertificate);
    }

    private void validateAllSources(CSARArchive csar, Path csarRootDirectory)
        throws NoSuchAlgorithmException, IOException {
        final CSARArchive.Manifest manifest = csar.getManifest();
        validateSources(csarRootDirectory, manifest);

        final Map<String, Map<String, List<String>>> nonMano = manifest.getNonMano();
        final List<SourcesParser.Source> sources = manifest.getSources();

        validateNonManoCohesionWithSources(nonMano, sources);
    }

    private void validateNonManoCohesionWithSources(final Map<String, Map<String, List<String>>> nonMano,
                                                    final List<SourcesParser.Source> sources) {

        final Collection<Map<String, List<String>>> values = nonMano.values();
        final List<String> nonManoSourcePaths = values.stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .flatMap(List::stream)
            .filter(it -> !it.isEmpty())
            .collect(Collectors.toList());

        final List<String> sourcePaths = sources.stream()
            .map(SourcesParser.Source::getValue)
            .collect(Collectors.toList());

        if (!sourcePaths.containsAll(nonManoSourcePaths)) {
            this.errors.add(new CSARErrorContentMismatch());
        }

    }

    private void validateFileSignature(CmsSignatureData signatureData) {
        final boolean isValid = this.manifestFileSignatureValidator.isValid(signatureData);
        if (!isValid) {
            this.errors.add(new CSARErrorInvalidSignature());
        }
    }

    private void validateSources(Path csarRootDirectory, CSARArchive.Manifest manifest)
        throws NoSuchAlgorithmException, IOException {
        final List<SourcesParser.Source> sources = manifest.getSources();
        for (SourcesParser.Source source : sources) {
            if (!source.getAlgorithm().isEmpty() || !source.getHash().isEmpty()) {
                validateSource(csarRootDirectory, source);
            }
        }
    }

    private void validateSource(Path csarRootDirectory, SourcesParser.Source source)
        throws NoSuchAlgorithmException, IOException {
        final Path sourcePath = csarRootDirectory.resolve(source.getValue());
        if (!sourcePath.toFile().exists()) {
            this.errors.add(new CSARErrorUnableToFindSource(source.getValue()));
        } else {
            if (!source.getAlgorithm().isEmpty()) {
                validateSourceHashCode(csarRootDirectory, source);
            } else if (source.getAlgorithm().isEmpty() && !source.getHash().isEmpty()) {
                this.errors.add(new CSARErrorUnableToFindAlgorithm(source.getValue()));
            }
        }
    }

    private void validateSourceHashCode(Path csarRootDirectory, SourcesParser.Source source)
        throws NoSuchAlgorithmException, IOException {
        String hashCode = generateHashCode(csarRootDirectory, source);
        if (!hashCode.equals(source.getHash())) {
            this.errors.add(new CSARErrorWrongHashCode(source.getValue()));
        }
    }

    private String generateHashCode(Path csarRootDirectory, SourcesParser.Source source)
        throws NoSuchAlgorithmException, IOException {
        final byte[] sourceData = Files.readAllBytes(csarRootDirectory.resolve(source.getValue()));
        final String algorithm = source.getAlgorithm();

        if (algorithm.equalsIgnoreCase(SHA_256)) {
            return this.shaHashCodeGenerator.generateSha256(sourceData);
        } else if (algorithm.equalsIgnoreCase(SHA_512)) {
            return this.shaHashCodeGenerator.generateSha512(sourceData);
        }

        throw new UnsupportedOperationException(String.format("Algorithm '%s' is not supported!", algorithm));
    }

    @Override
    protected String getVnfReqsNo() {
        return "R130206";
    }


    static class ManifestFileSignatureValidator {

        private final ManifestFileSplitter manifestFileSplitter = new ManifestFileSplitter();
        private final CmsSignatureValidator cmsSignatureValidator = new CmsSignatureValidator();
        private final CmsSignatureDataFactory cmsSignatureDataFactory = new CmsSignatureDataFactory();

        CmsSignatureData createSignatureData(File manifestFile) throws CmsSignatureLoadingException {
            ManifestFileModel mf = manifestFileSplitter.split(manifestFile);
            return cmsSignatureDataFactory.createForFirstSigner(
                toBytes(mf.getCMS(), mf.getNewLine()),
                toBytes(mf.getData(), mf.getNewLine())
            );
        }

        boolean isValid(CmsSignatureData signatureData) {
            try {
                return cmsSignatureValidator.verifySignedData(signatureData);
            } catch (CmsSignatureValidatorException e) {
                LOG.error("Unable to verify signed data!", e);
                return false;
            }
        }

        private byte[] toBytes(List<String> data, String newLine) {
            final String updatedData = data.stream().map(it -> it + newLine).collect(Collectors.joining());
            return updatedData.getBytes(Charset.defaultCharset());
        }
    }

}
