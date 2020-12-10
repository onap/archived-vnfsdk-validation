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

package org.onap.cvc.csar.parser;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.PnfCSARError;

import java.util.ArrayList;
import java.util.List;

import static org.onap.cvc.csar.parser.ManifestConsts.*;

public class MetadataParser {

    private final String fileName;

    public MetadataParser(String fileName) {
        this.fileName = fileName;
    }

    public Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> parse(List<String> lines) {
        CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isMetadataSectionAvailable = false;

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            ManifestLine manifestLine = ManifestLine.of(line);
            Pair<String, String> data = manifestLine.parse();

            if (data.getKey().equalsIgnoreCase(METADATA_SECTION_TAG_SECTION)) {
                isMetadataSectionAvailable = true;
            } else if (isMetadataSectionAvailable && !isLineExcluded(manifestLine)) {

                if (shouldStopProcessing(data, errors, lineNumber)) {
                    break;
                }

                handleMetadataLine(metadata, errors, lineNumber, data);
            }
        }

        if (!isMetadataSectionAvailable) {
            errors.add(new PnfCSARError.PnfCSARErrorEntryMissing(METADATA_SECTION_TAG_SECTION, this.fileName, -1));
        }

        return Pair.of(metadata, errors);

    }

    private boolean isLineExcluded(ManifestLine line) {
        return line.isEmpty()
                || line.startsWith("#")
                || line.startsWith(SOURCE_TAG_SECTION);
    }

    private boolean shouldStopProcessing(Pair<String, String> data, List<CSARArchive.CSARError> errors, int lineNumber) {
        if (isNewSection(data) || isSourceSection(data)) {
            if (!isSectionSupported(data.getKey())) {
                errors.add(new PnfCSARError.PnfCSARErrorWarning(data.getKey(), this.fileName, lineNumber));
            }
            return true;
        }
        return false;
    }

    private boolean isNewSection(Pair<String, String> data) {
        String key = data.getKey().trim();
        String value = data.getValue().trim();
        return key.matches("[a-zA-Z_0-9]+") && (value.isEmpty() || ManifestLine.of(value).startsWith("#"));
    }

    private boolean isSourceSection(Pair<String, String> data) {
        return data.getKey().equalsIgnoreCase(SOURCE_TAG_SECTION)
                || data.getKey().equalsIgnoreCase(ALGORITHM)
                || data.getKey().equalsIgnoreCase(HASH)
                || data.getKey().equalsIgnoreCase(SIGNATURE)
                || data.getKey().equalsIgnoreCase(CERTIFICATE);
    }

    private boolean isSectionSupported(String key) {
        return Lists.newArrayList(
                METADATA_SECTION_TAG_SECTION,
                SOURCE_TAG_SECTION, ALGORITHM, HASH, SIGNATURE, CERTIFICATE,
                NON_MANO_ARTIFACT_SETS_TAG_SECTION).contains(key.toLowerCase());
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
                errors.add(new PnfCSARError.PnfCSARErrorInvalidEntry(
                        paramName,
                        this.fileName,
                        lineNumber));
                break;
        }
    }

}
