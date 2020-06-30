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

    private final ShaHashCodeGenerator shaHashCodeGenerator = new ShaHashCodeGenerator();
    private final ManifestFileSignatureValidator manifestFileSignatureValidator = new ManifestFileSignatureValidator();

    public static class CSARErrorUnableToFindCertificate extends CSARArchive.CSARError {

        CSARErrorUnableToFindCertificate(String paramName) {
            super("0x4001");
            this.message = String.format("Unable to find cert file defined by %s!", paramName);
        }
    }

    public static class CSARErrorUnableToFindCmsSection extends CSARArchive.CSARError {

        CSARErrorUnableToFindCmsSection() {
            super("0x4002");
            this.message = "Unable to find CMS section in manifest!";
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
            this.message = "File has invalid CMS signature!";
        }
    }

    public static class CSARErrorContentMismatch extends CSARArchive.CSARError {

        CSARErrorContentMismatch() {
            super("0x4008");
            this.message = "Mismatch between contents of non-mano-artifact-sets and source files of the package";
        }
    }

    public static class CSARWarningNoSecurity extends CSARArchive.CSARErrorWarning{
        CSARWarningNoSecurity(){
            super("","",-1,"");
            this.message = "Warning. Consider adding security options (CMS and hash codes for sources) in manifest file.";
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
        final CSARArchive.Manifest manifest = csar.getManifest();
        validateEntryCertificate(csar, csarRootDirectory);
        if(verifyThatCsarIsSecure(manifest)){

            validateManifestCms(manifest);
            validateSources(csarRootDirectory, manifest);

            final Map<String, Map<String, List<String>>> nonMano = manifest.getNonMano();
            final List<SourcesParser.Source> sources = manifest.getSources();

            validateNonManoCohesionWithSources(nonMano, sources);

            final File manifestMfFile = csar.getManifestMfFile();
            final String absolutePathToEntryCertificate = getAbsolutePathToEntryCertificate(csar, csarRootDirectory);
            if (manifestMfFile != null) {
                validateFileSignature(manifestMfFile, absolutePathToEntryCertificate);
            }
        }else{
            this.errors.add(new CSARWarningNoSecurity());
        }

    }

    private boolean verifyThatCsarIsSecure(CSARArchive.Manifest manifest) {
        final List<SourcesParser.Source> sources = manifest.getSources();
        final String cms = manifest.getCms();
        final boolean containsHashOrAlgorithm = (sources.stream().anyMatch(source -> (!source.getAlgorithm().equals("") || !source.getHash().equals(""))));
        final boolean containsCms = (cms != null && !cms.equals(""));
        return containsCms || containsHashOrAlgorithm;
    }

    private String getAbsolutePathToEntryCertificate(CSARArchive csar, Path csarRootDirectory) {
        final String entryCertificateFileName = csar.getToscaMeta().getEntryCertificate();
        return String.format("%s/%s", csarRootDirectory.toAbsolutePath(), entryCertificateFileName);
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

    private void validateFileSignature(File manifestMfFile, String absolutePathToEntryCertificate) {
        final boolean isValid = this.manifestFileSignatureValidator.isValid(manifestMfFile, absolutePathToEntryCertificate);
        if (!isValid) {
            this.errors.add(new CSARErrorInvalidSignature());
        }
    }

    private void validateEntryCertificate(CSARArchive csar, Path csarRootDirectory) {
        final CSARArchive.Manifest manifest = csar.getManifest();
        final CSARArchive.TOSCAMeta toscaMeta = csar.getToscaMeta();
        final String entryCertificateParamName = csar.getEntryCertificateParamName();
        final Optional<File> entryCertificate = resolveCertificateFilePath(toscaMeta, csarRootDirectory);
        if (!entryCertificate.isPresent() || !entryCertificate.get().exists()) {
            this.errors.add(new CSARErrorUnableToFindCertificate(entryCertificateParamName));
        }


    }

    private void validateManifestCms(CSARArchive.Manifest manifest) {
        if (manifest.getCms() == null || manifest.getCms().isEmpty()) {
            this.errors.add(new CSARErrorUnableToFindCmsSection());
        }
    }

    private Optional<File> resolveCertificateFilePath(CSARArchive.TOSCAMeta toscaMeta, Path csarRootDirectory) {
        final String certificatePath = toscaMeta.getEntryCertificate();
        if (certificatePath == null) {
            return Optional.empty();
        } else {
            return Optional.of(csarRootDirectory.resolve(certificatePath).toFile());
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


    class ManifestFileSignatureValidator {

        private final Logger LOG = LoggerFactory.getLogger(ManifestFileSignatureValidator.class);
        private final ManifestFileSplitter manifestFileSplitter = new ManifestFileSplitter();
        private final CmsSignatureValidator cmsSignatureValidator = new CmsSignatureValidator();

        boolean isValid(File manifestFile, String absolutePathToEntryCertificate) {
            try {
                byte[] entryCertificate = Files.readAllBytes(new File(absolutePathToEntryCertificate).toPath());
                ManifestFileModel mf = manifestFileSplitter.split(manifestFile);
                return cmsSignatureValidator.verifySignedData(toBytes(mf.getCMS(), mf.getNewLine()),
                        Optional.of(entryCertificate),
                        toBytes(mf.getData(), mf.getNewLine()));
            } catch (CmsSignatureValidatorException e) {
                LOG.error("Unable to verify signed data!", e);
                return false;
            } catch (IOException e) {
                LOG.error("Unable to read ETSI entry certificate file!", e);
                return false;
            }
        }

        private byte[] toBytes(List<String> data, String newLine) {
            final String updatedData = data.stream().map(it -> it + newLine).collect(Collectors.joining());
            return updatedData.getBytes(Charset.defaultCharset());
        }
    }
}
