/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 * Modified 2020 Nokia.
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

package org.onap.cvc.csar.cc.sol004;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnapCommandSchema(schema = "vtp-validate-csar-r01123.yaml")
public class VTPValidateCSARR01123 extends VTPValidateCSARBase {

    public static class CSARErrorEntryVNFProviderDetailsNotFound extends CSARErrorEntryMissing {
        public CSARErrorEntryVNFProviderDetailsNotFound() {
            super("VNF Vendor details",
                CSARArchive.TOSCA_METADATA + " or " + CSARArchive.TOSCA_METADATA_TOSCA_META_ENTRY_DEFINITIONS + " file");
            this.setCode("0x1000");
        }
    }

    public static class CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest extends CSARErrorEntryMissing {
        CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest(List<String> fileInCsarThatAreNotLocatedInManifest) {
            super("Source",
                CSARArchive.TOSCA_METADATA);
            this.setCode("0x1010");
            this.message = "files: "
                + String.join(", ", fileInCsarThatAreNotLocatedInManifest)
                + " are located in CSAR, but are not located in Manifest as Source";
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        if (csar.getVendorName() == null ||
            csar.getVersion() == null) {
            errors.add(new CSARErrorEntryVNFProviderDetailsNotFound());
        }

        Path rootFolder = csar.getWorkspace().getRootFolder()
            .orElseThrow(() -> new IOException("Couldn't find CSAR root catalog"));
        List<String> filesInCsar = getAllFilesInDirectory(rootFolder);
        List<String> sourcesInManifest = getAllFilesFromManifestSources(csar.getManifest());

        if (filesInCsar.size() != sourcesInManifest.size() || !sourcesInManifest.containsAll(filesInCsar)) {
            filesInCsar.removeAll(sourcesInManifest);
            errors.add(new VTPValidateCSARR01123.CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest(filesInCsar));
        }
    }

    private List<String> getAllFilesFromManifestSources(CSARArchive.Manifest manifest) {
        return manifest.getSources()
            .stream()
            .map(SourcesParser.Source::getValue)
            .filter(filterOutManifestFile())
            .collect(Collectors.toList());
    }

    private List<String> getAllFilesInDirectory(Path rootPath) throws IOException {
        try (Stream<Path> paths = Files.walk(rootPath, Integer.MAX_VALUE)) {
            return paths
                .filter(filterOutDirectories())
                .map(rootPath::relativize)
                .map(String::valueOf)
                .filter(filterOutManifestFile())
                .collect(Collectors.toList());
        }
    }

    private Predicate<Path> filterOutDirectories() {
        return path -> !Files.isDirectory(path);
    }


    private Predicate<String> filterOutManifestFile() {
        return path -> !path.endsWith(".mf");
    }

    @Override
    protected String getVnfReqsNo() {
        return "R01123";
    }


}
