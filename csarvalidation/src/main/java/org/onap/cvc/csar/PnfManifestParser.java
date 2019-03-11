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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PnfManifestParser {


    public static final String METADATA_SECTION = "metadata";
    public static final String NON_MANO_ARTIFACT_SETS_SECTION = "non_mano_artifact_sets";

    private final List<String> lines;
    private final String fileName;

    PnfManifestParser(List<String> lines, String fileName) {
        this.lines = lines;
        this.fileName = fileName;
    }

    public static PnfManifestParser getInstance(String fileName) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream
                    .map(String::trim)
                    .collect(Collectors.toList());

            return new PnfManifestParser(lines, fileName);
        }
    }

    public Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> fetchMetadata() {
        CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isMetadataSectionAvailable = false;
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.trim().isEmpty()){
                continue;
            } else if (line.startsWith(METADATA_SECTION)) {
                isMetadataSectionAvailable = true;
                continue;
            }else if (isMetadataSectionAvailable) {
                Pair<String, String> data = parseLine(line);

                if (isNewSection(data)) {
                    if(!isSectionSupported(data.getKey())) {
                        errors.add(new CSARArchive.CSARErrorWarning(data.getKey(), this.fileName, lineNumber, null));
                    }
                    break;
                }

                handleMetadataLine(metadata, errors, lineNumber, data);
            }
        }

        if (!isMetadataSectionAvailable) {
            errors.add(new CSARArchive.CSARErrorEntryMissing(METADATA_SECTION, this.fileName, -1, null));
        }

        return Pair.of(metadata, errors);

    }

    public Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>> fetchNonManoArtifacts() {
        Map<String, Map<String, List<String>>> nonManoArtifacts = new HashMap<>();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isNonManoArtifactsSectionAvailable = false;
        String attributeName = null;

        for (String line : lines) {

            if (line.trim().isEmpty()) {
                continue;
            } else if (line.startsWith(NON_MANO_ARTIFACT_SETS_SECTION)) {
                isNonManoArtifactsSectionAvailable = true;
                continue;
            } else if (isNonManoArtifactsSectionAvailable) {
                Pair<String, String> data = parseLine(line);

                if (isNewSection(data)) {
                    attributeName = data.getKey();
                    continue;
                }

                handleNonManoArtifactLine(nonManoArtifacts, attributeName, data);
            }
        }

        if (!isNonManoArtifactsSectionAvailable) {
            errors.add(new CSARArchive.CSARErrorEntryMissing(NON_MANO_ARTIFACT_SETS_SECTION, this.fileName, -1, null));
        }

        return Pair.of(nonManoArtifacts, errors);
    }

    private void handleMetadataLine(
            CSARArchive.Manifest.Metadata metadata,
            List<CSARArchive.CSARError> errors,
            int lineNumber,
            Pair<String, String> data) {

        String paramName = data.getKey();
        String value = data.getValue();

        switch (paramName) {
            case "pnf_product_name":
                metadata.setProductName(value);
                break;
            case "pnf_provider_id":
                metadata.setProviderId(value);
                break;
            case "pnf_package_version":
                metadata.setPackageVersion(value);
                break;
            case "pnf_release_date_time":
                metadata.setReleaseDateTime(value);
                break;
            default:
                errors.add(new CSARArchive.CSARErrorInvalidEntry(
                        paramName,
                        this.fileName,
                        lineNumber,
                        null));
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
        return Lists.newArrayList(METADATA_SECTION, NON_MANO_ARTIFACT_SETS_SECTION).contains(key);
    }


    private boolean isNewSection(Pair<String, String> data) {
        String value = data.getValue().trim();
        return value.isEmpty() || value.startsWith("#");
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
