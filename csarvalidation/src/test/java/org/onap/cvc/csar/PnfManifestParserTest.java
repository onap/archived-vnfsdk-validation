package org.onap.cvc.csar;

import com.google.common.collect.Lists;
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
        CSARArchive.Manifest.Metadata metadata = pnfManifestParser.fetchMetadata();

        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
    }


    @Test
    public void shouldFetchNonManoArtifactsFromFile() {
        Map<String, Map<String, List<String>>> nonManoArtifacts = pnfManifestParser.fetchNonManoArtifacts();

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


}