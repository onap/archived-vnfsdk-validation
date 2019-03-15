/**
 * Copyright 2019 Nokia
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
package org.onap.cvc.csar;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PnfCSARArchiveTest {

    @Test
    public void shouldUseDataStoredInManifestMfFileToConfigurePnfCSARArchive() throws IOException, URISyntaxException {
        // given
        String fileName = PnfCSARArchiveTest.class.getClassLoader().getResource("pnf/dummyPnfv2.csar")
                .toURI().getPath();
        PnfCSARArchive pnfCSARArchive = new PnfCSARArchive();
        pnfCSARArchive.init(fileName);

        // when
        try {
            pnfCSARArchive.parse();
        }finally {
            pnfCSARArchive.cleanup();
        }

        // then
        verifyThatMetadataWasSet(pnfCSARArchive);
        verifyThatNonManoArtifactsWereSet(pnfCSARArchive);
    }

    private void verifyThatNonManoArtifactsWereSet(PnfCSARArchive pnfCSARArchive) {
        Map<String, Map<String, List<String>>> nonManoArtifacts = pnfCSARArchive.getManifest().getNonMano();
        assertThat(nonManoArtifacts.get("onap_ves_events").get("source"))
                .isEqualTo(Lists.newArrayList("Artifacts/Events/VES_registration.yml")
                );
        assertThat(nonManoArtifacts.get("onap_pm_dictionary").get("source"))
                .isEqualTo(Lists.newArrayList("Artifacts/Measurements/PM_Dictionary.yaml")
                );
        assertThat(nonManoArtifacts.get("onap_yang_module").get("source"))
                .isEqualTo(Lists.newArrayList("Artifacts/Yang_module/Yang_module.yaml")
                );
        assertThat(nonManoArtifacts.get("onap_others").get("source"))
                .isEqualTo(Lists.newArrayList(
                        "Artifacts/scripts/install.sh",
                        "Artifacts/Informational/user_guide.txt",
                        "Artifacts/Other/installation_guide.txt",
                        "Artifacts/Other/review_log.txt"
                        )
                );
    }

    private void verifyThatMetadataWasSet(PnfCSARArchive pnfCSARArchive) {
        CSARArchive.Manifest.Metadata metadata = pnfCSARArchive.getManifest().getMetadata();
        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
    }

}