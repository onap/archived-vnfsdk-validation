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

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.CSARArchive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.onap.cvc.csar.parser.ManifestConsts.BEGIN_CMS_SECTION;
import static org.onap.cvc.csar.parser.ManifestConsts.NON_MANO_ARTIFACT_SETS_TAG_SECTION;

public class NonManoArtifactsParser {


    public Optional<Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>>> parse(List<String> lines) {
        Map<String, Map<String, List<String>>> nonManoArtifacts = new HashMap<>();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isNonManoArtifactsSectionAvailable = false;
        String attributeName = null;

        for (String line : lines) {
            ManifestLine manifestLine = ManifestLine.of(line);
            if (manifestLine.startsWith(NON_MANO_ARTIFACT_SETS_TAG_SECTION)) {
                isNonManoArtifactsSectionAvailable = true;
            } else if (manifestLine.contains(BEGIN_CMS_SECTION)) {
                break;
            } else if (isNonManoArtifactsSectionAvailable) {
                Pair<String, String> data = manifestLine.parse();

                if (isNewSection(data)) {
                    attributeName = data.getKey();
                    nonManoArtifacts.put(attributeName, new HashMap<>());
                } else{
                handleNonManoArtifactLine(nonManoArtifacts, attributeName, data);
                }
            }
        }

        if (!isNonManoArtifactsSectionAvailable) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(nonManoArtifacts, errors));
    }

    private boolean isNewSection(Pair<String, String> data) {
        String key = data.getKey().trim();
        String value = data.getValue().trim();
        return key.matches("[a-zA-z_0-9]+") && (value.isEmpty() || ManifestLine.of(value).startsWith("#"));
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

}
