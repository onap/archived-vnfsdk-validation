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
import org.onap.cvc.csar.PnfCSARArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.parser.SourcesParser;
import org.onap.cvc.csar.security.ShaHashCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@OnapCommandSchema(schema = "vtp-validate-csar-r787966.yaml")
public class VTPValidateCSARR787966 extends VTPValidateCSARBase {

    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR787966.class);
    private static final String SHA_256 = "SHA-256";
    private static final String SHA_512 = "SHA-512";

    private final ShaHashCodeGenerator shaHashCodeGenerator = new ShaHashCodeGenerator();


    public static class CSARErrorUnableToFindCertificate extends CSARArchive.CSARError {
        CSARErrorUnableToFindCertificate() {
            super("0x4001");
            this.message = "Unable to find cert file defined by Entry-Certificate!";
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

    @Override
    protected void validateCSAR(CSARArchive csar) throws OnapCommandException {

        try {
            FileArchive.Workspace workspace = csar.getWorkspace();
            final Optional<Path> pathToCsarFolder = workspace.getPathToCsarFolder();
            if(pathToCsarFolder.isPresent()) {
                validate(csar, pathToCsarFolder.get());
            } else {
                this.errors.add(new CSARErrorUnableToFindCsarContent());
            }
        } catch (Exception e) {
            LOG.error("Internal VTPValidateCSARR787966 command error", e);
            throw new OnapCommandException("0x3000", "Internal VTPValidateCSARR787966 command error. See logs.");
        }

    }

    private void validate(CSARArchive csar, Path csarRootDirectory ) throws IOException, NoSuchAlgorithmException {

        final PnfCSARArchive.PnfManifest manifest = (PnfCSARArchive.PnfManifest) csar.getManifest();
        final CSARArchive.TOSCAMeta toscaMeta = csar.getToscaMeta();
        validateSecurityStructure(toscaMeta, csarRootDirectory, manifest);
        validateSources(csarRootDirectory, manifest);
    }

    private void validateSecurityStructure(CSARArchive.TOSCAMeta toscaMeta , Path csarRootDirectory, PnfCSARArchive.PnfManifest manifest) {
        final File entryCertificate = csarRootDirectory.resolve(toscaMeta.getEntryCertificate()).toFile();
        if (!entryCertificate.exists() && !manifest.getCms().isEmpty()) {
            this.errors.add(new CSARErrorUnableToFindCertificate());
        } else if (entryCertificate.exists() && manifest.getCms().isEmpty()) {
            this.errors.add(new CSARErrorUnableToFindCmsSection());
        }
    }

    private void validateSources(Path csarRootDirectory, PnfCSARArchive.PnfManifest manifest) throws NoSuchAlgorithmException, IOException {
        final List<SourcesParser.Source> sources = manifest.getSources();
        for (SourcesParser.Source source: sources){
            if(!source.getAlgorithm().isEmpty()) {
                validateSourceHashCode(csarRootDirectory, source);
            } else if(source.getAlgorithm().isEmpty() && !source.getHash().isEmpty()){
                this.errors.add(new CSARErrorUnableToFindAlgorithm(source.getValue()));
            }
        }
    }

    private void validateSourceHashCode(Path csarRootDirectory, SourcesParser.Source source) throws NoSuchAlgorithmException, IOException {
        String hashCode = generateHashCode(csarRootDirectory, source);
        if (!hashCode.equals(source.getHash())) {
            this.errors.add(new CSARErrorWrongHashCode(source.getValue()));
        }
    }

    private String generateHashCode(Path csarRootDirectory, SourcesParser.Source source) throws NoSuchAlgorithmException, IOException {
        final byte[] sourceData = Files.readAllBytes(csarRootDirectory.resolve(source.getValue()));
        final String algorithm = source.getAlgorithm();

        if(algorithm.equalsIgnoreCase(SHA_256)) {
            return this.shaHashCodeGenerator.generateSha256(sourceData);
        } else if(algorithm.equalsIgnoreCase(SHA_512)){
            return this.shaHashCodeGenerator.generateSha512(sourceData);
        }

        throw new UnsupportedOperationException(String.format("Algorithm '%s' is not supported!", algorithm));
    }

    @Override
    protected String getVnfReqsNo() {
        return "R787966";
    }


}
