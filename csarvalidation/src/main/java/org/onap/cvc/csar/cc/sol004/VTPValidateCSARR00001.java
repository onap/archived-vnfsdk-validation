/*
 * Copyright 2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.cvc.csar.cc.sol004;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.onap.cvc.csar.CSARArchive.Manifest;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnapCommandSchema(schema = "vtp-validate-csar-r00001.yaml")
public class VTPValidateCSARR00001 extends VTPValidateCSARBase {


    public static class CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest extends CSARErrorEntryMissing {
        CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest() {
            super("Source",
                CSARArchive.TOSCA_METADATA);
            this.setCode("0x1010");
            this.message = "Not all files, that are located in CSAR, are located in Manifest as Source";
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        Path rootFolder = csar.getWorkspace().getRootFolder()
            .orElseThrow( () -> new IOException("Couldn't find CSAR root catalog") );
        List<String> filesInCsar = getAllFilesInDirectory(rootFolder);
        List<String> sourcesInManifest = getAllFilesFromManifestSources(csar.getManifest());

        if (filesInCsar.size() != sourcesInManifest.size() ||  !sourcesInManifest.containsAll(filesInCsar)) {
            errors.add(new VTPValidateCSARR00001.CSARErrorNotAllFilesLocatedInCSARWhereListedInManifest());
        }
    }

    private List<String> getAllFilesFromManifestSources(Manifest manifest) {
        return manifest.getSources()
            .stream()
            .map(SourcesParser.Source::getValue)
            .filter(filterOutManifestFile())
            .collect(Collectors.toList());
    }

    private List<String> getAllFilesInDirectory(Path rootPath) throws IOException {
        try ( Stream<Path> paths = Files.walk(rootPath, Integer.MAX_VALUE) ) {
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
        return "R00001";
    }

}
