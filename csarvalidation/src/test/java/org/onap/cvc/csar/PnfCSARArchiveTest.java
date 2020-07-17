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
import org.onap.cvc.csar.parser.SourcesParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PnfCSARArchiveTest {

    private static final String SOURCE_TAG = "Source";

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
            CSARArchive.Manifest manifest = pnfCSARArchive.getManifest();
            verifyThatMetadataWasSet(manifest);
            verifyThatCmsSectionWasSet(manifest);
            verifyThatSourcesSectionWasSet(manifest);
            verifyThatNonManoArtifactsWereSet(manifest);
            assertThat(pnfCSARArchive.getErrors().size()).isZero();
        }

    }

    private void verifyThatMetadataWasSet(CSARArchive.Manifest manifest) {
        CSARArchive.Manifest.Metadata metadata = manifest.getMetadata();
        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
    }

    private void verifyThatSourcesSectionWasSet(CSARArchive.Manifest manifest) {

        List<SourcesParser.Source> sources = manifest.getSources();
        assertThat(sources).contains(
                new SourcesParser.Source("MRF.yaml", "SHA-256", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943"),
                new SourcesParser.Source("scripts/install.sh", "SHA-256", "d0e7828293355a07c2dccaaa765c80b507e60e6167067c950dc2e6b0da0dbd8b"),
                new SourcesParser.Source("https://www.vendor_org.com/MRF/v4.1/scripts/scale/scale.sh", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165")
        );
    }

    private void verifyThatCmsSectionWasSet(CSARArchive.Manifest manifest) {

        String cms = manifest.getCms();
        assertThat(cms).isEqualTo("MIGDBgsqhkiG9w0BCRABCaB0MHICAQAwDQYLKoZIhvcNAQkQAwgwXgYJKoZIhvcNAQcBoFEET3icc87PK0nNK9ENqSxItVIoSa0o0S/ISczMs1ZIzkgsKk4tsQ0N1nUMdvb05OXi5XLPLEtViMwvLVLwSE0sKlFIVHAqSk3MBkkBAJv0Fx0=");
    }

    private void verifyThatNonManoArtifactsWereSet(CSARArchive.Manifest manifest) {
        Map<String, Map<String, List<String>>> nonManoArtifacts = manifest.getNonMano();
        Map<String, List<String>>mapValue1=new HashMap<>();
        Map<String, List<String>>mapValue2=new HashMap<>();
        Map<String, List<String>>mapValue3=new HashMap<>();
        Map<String, List<String>>mapValue4=new HashMap<>();

        mapValue1.put("", Collections.singletonList(""));
        mapValue1.put(SOURCE_TAG,Lists.newArrayList("Artifacts/Events/VES_registration.yml"));
        mapValue2.put("", Collections.singletonList(""));
        mapValue2.put(SOURCE_TAG,Lists.newArrayList("Artifacts/Measurements/PM_Dictionary.yaml"));
        mapValue3.put("", Collections.singletonList(""));
        mapValue3.put(SOURCE_TAG,Lists.newArrayList("Artifacts/Yang_module/Yang_module.yaml"));
        mapValue4.put("", Collections.singletonList(""));
        mapValue4.put(SOURCE_TAG,Lists.newArrayList
                ("Artifacts/scripts/install.sh",
                        "Artifacts/Informational/user_guide.txt",
                        "Artifacts/Other/installation_guide.txt",
                        "Artifacts/Other/review_log.txt"));
        assertThat(nonManoArtifacts).containsEntry("onap_ves_events",mapValue1);
        assertThat(nonManoArtifacts).containsEntry("onap_pm_dictionary",mapValue2);
        assertThat(nonManoArtifacts).containsEntry("onap_yang_modules",mapValue3);
        assertThat(nonManoArtifacts).containsEntry("onap_others",mapValue4);
    }

}