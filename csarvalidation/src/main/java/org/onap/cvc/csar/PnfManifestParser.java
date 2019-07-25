/**
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
 */
package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.parser.CmsParser;
import org.onap.cvc.csar.parser.MetadataParser;
import org.onap.cvc.csar.parser.NonManoArtifactsParser;
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PnfManifestParser {

    private final List<String> lines;

    private final MetadataParser metadataParser;
    private final SourcesParser sourcesParser;
    private final NonManoArtifactsParser nonManoArtifactsParser;
    private final CmsParser cmsParser;


    PnfManifestParser(List<String> lines, MetadataParser metadataParser, SourcesParser sourcesParser, NonManoArtifactsParser nonManoArtifactsParser, CmsParser cmsParser) {
        this.lines = lines;
        this.metadataParser = metadataParser;
        this.sourcesParser = sourcesParser;
        this.nonManoArtifactsParser = nonManoArtifactsParser;
        this.cmsParser = cmsParser;
    }

    static PnfManifestParser getInstance(File pnfManifestFile) throws IOException {
        String fileName = pnfManifestFile.getAbsolutePath();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream
                    .map(String::trim)
                    .collect(Collectors.toList());

            final String pnfManifestFileName = pnfManifestFile.getName();
            return new PnfManifestParser(lines, new MetadataParser(pnfManifestFileName), new SourcesParser(pnfManifestFileName), new NonManoArtifactsParser(), new CmsParser(pnfManifestFileName));
        }
    }

    Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> fetchMetadata() {
        return this.metadataParser.parse(this.lines);

    }

    Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> fetchSourcesSection() {
        return this.sourcesParser.parse(this.lines);
    }


    Optional<Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>>> fetchNonManoArtifacts() {
        return this.nonManoArtifactsParser.parse(this.lines);
    }


    Pair<String, List<CSARArchive.CSARError>> fetchCMS() {
        return this.cmsParser.parse(this.lines);
    }

}
