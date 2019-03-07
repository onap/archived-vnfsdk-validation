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
    public void shouldUseDataStoredInManifestMfFileToConfigure() throws IOException, URISyntaxException {
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
    }

    private void verifyThatMetadataWasSet(PnfCSARArchive pnfCSARArchive) {
        CSARArchive.Manifest.Metadata metadata = pnfCSARArchive.getManifest().getMetadata();
        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
    }

}