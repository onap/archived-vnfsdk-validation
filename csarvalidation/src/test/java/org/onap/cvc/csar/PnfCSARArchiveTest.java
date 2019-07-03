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
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PnfCSARArchiveTest {

    public static final String SOURCE_TAG = "Source";

    @Test
    public void shouldUseDataStoredInManifestMfFileToConfigurePnfCSARArchive() throws Exception {
        // given
        String fileName = PnfCSARArchiveTest.class.getClassLoader().getResource("pnf/dummyPnfv2.csar")
                .toURI().getPath();

        // when
        try (PnfCSARArchive pnfCSARArchive = new PnfCSARArchive()) {
            pnfCSARArchive.init(fileName);
            pnfCSARArchive.parse();
            // then
            PnfCSARArchive.PnfManifest manifest = (PnfCSARArchive.PnfManifest) pnfCSARArchive.getManifest();
            verifyThatMetadataWasSet(manifest);
            verifyThatSourcesSectionWasSet(manifest);
            verifyThatNonManoArtifactsWereSet(manifest);
        }

    }

    private void verifyThatMetadataWasSet(PnfCSARArchive.PnfManifest manifest) {
        CSARArchive.Manifest.Metadata metadata = manifest.getMetadata();
        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
    }

    private void verifyThatSourcesSectionWasSet(PnfCSARArchive.PnfManifest manifest) {

        List<String> sources = manifest.getSources();
        assertThat(sources).contains("Definitions/MainServiceTemplate.yaml", "Definitions/etsi_nfv_sol001_vnfd_2_5_1_types.yaml");
    }

    private void verifyThatNonManoArtifactsWereSet(PnfCSARArchive.PnfManifest manifest) {
        Map<String, Map<String, List<String>>> nonManoArtifacts = manifest.getNonMano();
        assertThat(nonManoArtifacts.get("onap_ves_events").get(SOURCE_TAG))
                .isEqualTo(Lists.newArrayList("Artifacts/Events/VES_registration.yml")
                );
        assertThat(nonManoArtifacts.get("onap_pm_dictionary").get(SOURCE_TAG))
                .isEqualTo(Lists.newArrayList("Artifacts/Measurements/PM_Dictionary.yaml")
                );
        assertThat(nonManoArtifacts.get("onap_yang_modules").get(SOURCE_TAG))
                .isEqualTo(Lists.newArrayList("Artifacts/Yang_module/Yang_module.yaml")
                );
        assertThat(nonManoArtifacts.get("onap_others").get(SOURCE_TAG))
                .isEqualTo(Lists.newArrayList(
                        "Artifacts/scripts/install.sh",
                        "Artifacts/Informational/user_guide.txt",
                        "Artifacts/Other/installation_guide.txt",
                        "Artifacts/Other/review_log.txt"
                        )
                );
    }

}