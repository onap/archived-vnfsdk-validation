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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class PnfManifestParserTest {

    private PnfManifestParser pnfManifestParser;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        String fileName = PnfManifestParserTest.class.getClassLoader().getResource("pnf/MainServiceTemplate.mf")
                .toURI().getPath();
        pnfManifestParser = PnfManifestParser.getInstance(fileName);
    }

    @Test
    public void shouldFetchMetadataFromFile() {
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> metadataListPair = pnfManifestParser.fetchMetadata();
        CSARArchive.Manifest.Metadata metadata = metadataListPair.getKey();
        List<CSARArchive.CSARError> errors= metadataListPair.getValue();

        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldFetchNonManoArtifactsFromFile() {
        Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>> mapListPair = pnfManifestParser.fetchNonManoArtifacts();
        Map<String, Map<String, List<String>>> nonManoArtifacts = mapListPair.getKey();
        List<CSARArchive.CSARError> errors= mapListPair.getValue();

        assertThat(nonManoArtifacts.get("prv.onap.ves_event").get("Source"))
                .isEqualTo(Lists.newArrayList("Artifacts/Deployment/Events/RadioNode_Pnf_v1.yml")
                );
        assertThat(nonManoArtifacts.get("prv.onap.pm_dictionary").get("Source"))
                .isEqualTo(Lists.newArrayList("Artifacts/Deployment/Measurements/PM_Dictionary.yml")
                );
        assertThat(nonManoArtifacts.get("prv.onap.yang_modules").get("Source"))
                .isEqualTo(Lists.newArrayList(
                        "Artifacts/Deployment/Yang_module/yang-module1.yang",
                        "Artifacts/Deployment/Yang_module/yang-module2.yang"
                        )
                );
        assertThat(errors.size()).isEqualTo(0);
    }
}