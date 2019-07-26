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

package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.parser.CmsParser;
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class VnfManifestParser {

    private static final String ENTRY_MANIFEST_METADATA = "metadata";
    private static final String ENTRY_MANIFEST_METADATA_VNF_PROVIDER_ID = "vnf_provider_id";
    private static final String ENTRY_MANIFEST_METADATA_VNF_PRODUCT_NAME = "vnf_product_name";
    private static final String ENTRY_MANIFEST_METADATA_VNF_RELEASE_DATA_TIME = "vnf_release_data_time";
    private static final String ENTRY_MANIFEST_METADATA_VNF_PACKAGE_VERSION = "vnf_package_version";

    private final List<String> lines;
    private final String vnfManifestFileName;
    private final SourcesParser sourcesParser;
    private final CmsParser cmsParser;

    private VnfManifestParser(List<String> lines, String vnfManifestFileName, SourcesParser sourcesParser, CmsParser cmsParser) {
        this.lines = lines;
        this.vnfManifestFileName = vnfManifestFileName;
        this.sourcesParser = sourcesParser;
        this.cmsParser = cmsParser;
    }

    static VnfManifestParser getInstance(File vnfManifestFile) throws IOException {
        String fileName = vnfManifestFile.getAbsolutePath();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream
                    .map(String::trim)
                    .collect(Collectors.toList());

            final String vnfManifestFileName = vnfManifestFile.getName();
            return new VnfManifestParser(
                    lines,
                    vnfManifestFileName,
                    new SourcesParser(vnfManifestFileName),
                    new CmsParser(vnfManifestFileName)
            );
        }
    }


    Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> fetchMetadata(){

        final CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        final List<CSARArchive.CSARError> errors = new ArrayList<>();

        int lineNo =0;

        //first hit the metadata: section
        for (String line: lines) {
            lineNo ++;
            line = line.trim();

            //continue till it reaches the metadata section
            if (line.equalsIgnoreCase(ENTRY_MANIFEST_METADATA + ":")) {
                break;
            }
        }

        if (lineNo < lines.size()) {
            parseMetadataSection(metadata, errors, lineNo);
        }

        return Pair.of(metadata, errors);
    }

    private void parseMetadataSection(CSARArchive.Manifest.Metadata metadata, List<CSARArchive.CSARError> errors, int lineNo) {
        for (int i = lineNo; i< lines.size(); i++) {
            String line = lines.get(i).trim();

            String[] tokens = line.split(":");
            if (skipLine( line ) || tokens.length < 2){
                continue;
            }

            String key = tokens[0];
            String value = tokens[1];

            //continue till it reaches the metadata section
            if (key.equalsIgnoreCase(ENTRY_MANIFEST_METADATA_VNF_PACKAGE_VERSION)) {
                metadata.setPackageVersion(value);
            } else if (key.equalsIgnoreCase(ENTRY_MANIFEST_METADATA_VNF_PRODUCT_NAME)) {
                metadata.setProductName(value);
            } else if (key.equalsIgnoreCase(ENTRY_MANIFEST_METADATA_VNF_PROVIDER_ID)) {
                metadata.setProviderId(value);
            } else if (key.equalsIgnoreCase(ENTRY_MANIFEST_METADATA_VNF_RELEASE_DATA_TIME)) {
                metadata.setReleaseDateTime(value);
            } else {
                //Non-Mano entries are not processed as of now...
                errors.add(
                        new CSARArchive.CSARErrorIgnored(
                                key,
                                vnfManifestFileName,
                                i,
                                null));
            }
        }
    }

    Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> fetchSourcesSection() {
        return this.sourcesParser.parse(this.lines);
    }

    Pair<String, List<CSARArchive.CSARError>> fetchCMS() {
        return this.cmsParser.parse(this.lines);
    }

    private boolean skipLine(String line) {
        return line.startsWith("#")
                || line.isEmpty()
                || line.toLowerCase().startsWith("source")
                || line.toLowerCase().startsWith("algorithm")
                || line.toLowerCase().startsWith("hash");
    }
}
