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

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorEntryMissing;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorInvalidEntry;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorWarning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PnfManifestParser {


    private static final String METADATA_SECTION_TAG_SECTION = "metadata";
    private static final String SOURCE_TAG_SECTION = "source";
    private static final String NON_MANO_ARTIFACT_SETS_TAG_SECTION = "non_mano_artifact_sets";
    private static final String PRODUCT_NAME = "pnfd_name";
    private static final String PROVIDER_ID = "pnfd_provider";
    private static final String VERSION = "pnfd_archive_version";
    private static final String RELEASE_DATE_TIME = "pnfd_release_date_time";

    private final List<String> lines;
    private final String fileName;

    PnfManifestParser(List<String> lines, String fileName) {
        this.lines = lines;
        this.fileName = fileName;
    }

    static PnfManifestParser getInstance(File pnfManifestFile) throws IOException {
        String fileName = pnfManifestFile.getAbsolutePath();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream
                    .map(String::trim)
                    .collect(Collectors.toList());

            return new PnfManifestParser(lines, pnfManifestFile.getName());
        }
    }

    Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> fetchMetadata() {
        CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isMetadataSectionAvailable = false;

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            Pair<String, String> data = parseLine(line);

            if(data.getKey().toLowerCase().equals(METADATA_SECTION_TAG_SECTION)) {
                isMetadataSectionAvailable = true;
            }else if (isMetadataSectionAvailable && !isLineExcluded(line)) {

                if (shouldStopProcessing(data, errors, lineNumber)) {
                    break;
                }

                handleMetadataLine(metadata, errors, lineNumber, data);
            }
        }

        if (!isMetadataSectionAvailable) {
            errors.add(new PnfCSARErrorEntryMissing(METADATA_SECTION_TAG_SECTION, this.fileName, -1));
        }

        return Pair.of(metadata, errors);

    }

    Pair<List<String>, List<CSARArchive.CSARError>> fetchSourcesSection() {
        List<String> sources = new ArrayList<>();
        List<CSARArchive.CSARError> errors = new ArrayList<>();
        boolean isSpecialTagReached = false;
        boolean sourceSectionParsing = false;
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            if (sourceSectionParsing && (startsWith(line, METADATA_SECTION_TAG_SECTION) || startsWith(line, NON_MANO_ARTIFACT_SETS_TAG_SECTION))) {
                isSpecialTagReached = true;
            }else if (!isSpecialTagReached && startsWith(line, SOURCE_TAG_SECTION)) {
                sourceSectionParsing = true;
                Pair<String, String> data = parseLine(line);

                String value = data.getValue();
                if (value.isEmpty()) {
                    errors.add(new PnfCSARErrorWarning(data.getKey(), this.fileName, lineNumber));
                    break;
                } else {
                    sources.add(value);
                }
            }
        }

        return Pair.of(sources, errors);
    }

    Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>> fetchNonManoArtifacts() {
        Map<String, Map<String, List<String>>> nonManoArtifacts = new HashMap<>();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isNonManoArtifactsSectionAvailable = false;
        String attributeName = null;

        for (String line : lines) {

            if (startsWith(line, NON_MANO_ARTIFACT_SETS_TAG_SECTION)) {
                isNonManoArtifactsSectionAvailable = true;
            } else if (isNonManoArtifactsSectionAvailable) {
                Pair<String, String> data = parseLine(line);

                if (isNewSection(data)) {
                    attributeName = data.getKey();
                    nonManoArtifacts.put(attributeName, new HashMap<>());
                    continue;
                }

                handleNonManoArtifactLine(nonManoArtifacts, attributeName, data);
            }
        }

        if (!isNonManoArtifactsSectionAvailable) {
            errors.add(new PnfCSARErrorEntryMissing(NON_MANO_ARTIFACT_SETS_TAG_SECTION, this.fileName, -1));
        }

        return Pair.of(nonManoArtifacts, errors);
    }

    private boolean isLineExcluded(String line) {
        return line.trim().isEmpty()
                || startsWith(line, "#")
                || startsWith(line,SOURCE_TAG_SECTION);
    }

    private boolean shouldStopProcessing(Pair<String, String> data, List<CSARArchive.CSARError> errors, int lineNumber) {
        if (isNewSection(data) || data.getKey().toLowerCase().equals(SOURCE_TAG_SECTION)) {
            if(!isSectionSupported(data.getKey())) {
                errors.add(new PnfCSARErrorWarning(data.getKey(), this.fileName, lineNumber));
            }
            return true;
        }
        return false;
    }

    private boolean startsWith(String line, String word){
        return line.trim().toLowerCase().startsWith(word);
    }

    private void handleMetadataLine(
            CSARArchive.Manifest.Metadata metadata,
            List<CSARArchive.CSARError> errors,
            int lineNumber,
            Pair<String, String> data) {

        String paramName = data.getKey();
        String value = data.getValue();

        switch (paramName) {
            case PRODUCT_NAME:
                metadata.setProductName(value);
                break;
            case PROVIDER_ID:
                metadata.setProviderId(value);
                break;
            case VERSION:
                metadata.setPackageVersion(value);
                break;
            case RELEASE_DATE_TIME:
                metadata.setReleaseDateTime(value);
                break;
            default:
                errors.add(new PnfCSARErrorInvalidEntry(
                        paramName,
                        this.fileName,
                        lineNumber));
                break;
        }
    }

    private void handleNonManoArtifactLine(
            Map<String, Map<String, List<String>>> nonManoArtifacts,
            String attributeName,
            Pair<String, String> data) {

        String key = data.getKey();
        String value = data.getValue();

        Map<String, List<String>> attributeWithValues = nonManoArtifacts.getOrDefault(attributeName, new HashMap<>());
        List<String> values = attributeWithValues.getOrDefault(key, new ArrayList<>());
        values.add(value);
        attributeWithValues.put(key, values);
        nonManoArtifacts.put(attributeName, attributeWithValues);
    }

    private boolean isSectionSupported(String key) {
        return Lists.newArrayList(
                METADATA_SECTION_TAG_SECTION,
                SOURCE_TAG_SECTION,
                NON_MANO_ARTIFACT_SETS_TAG_SECTION).contains(key.toLowerCase());
    }


    private boolean isNewSection(Pair<String, String> data) {
        String key = data.getKey().trim();
        String value = data.getValue().trim();
        return key.matches("[a-zA-z_0-9]+") && (value.isEmpty() || startsWith(value,"#"));
    }


    private Pair<String, String> parseLine(String line) {
        String[] elements = line.split(": ");
        if (elements.length == 2)
            return Pair.of(elements[0], elements[1]);

        if (line.endsWith(":"))
            return Pair.of(line.substring(0, line.length() - 1), "");
        else
            return Pair.of(line, "");


    }
}
