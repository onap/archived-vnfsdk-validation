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
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class PnfManifestParserTest {

    private PnfManifestParser pnfManifestParser;

    @Before
    public void setUp() throws IOException {
        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplate.mf"));
    }

    @Test
    public void shouldFetchMetadataFromFile() {
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> metadataListPair = pnfManifestParser.fetchMetadata();
        CSARArchive.Manifest.Metadata metadata = metadataListPair.getKey();
        List<CSARArchive.CSARError> errors = metadataListPair.getValue();

        assertThat(metadata.getProductName()).isEqualTo("RadioNode");
        assertThat(metadata.getProviderId()).isEqualTo("Ericsson");
        assertThat(metadata.getPackageVersion()).isEqualTo("1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo("2019-01-14T11:25:00+00:00");
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldFetchSourcesSectionFromFile() {

        Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchSourcesSection();
        List<SourcesParser.Source> sources = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(sources).contains(
                new SourcesParser.Source("MRF.yaml", "SHA-256", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943"),
                new SourcesParser.Source("scripts/install.sh", "SHA-256", "d0e7828293355a07c2dccaaa765c80b507e60e6167067c950dc2e6b0da0dbd8b"),
                new SourcesParser.Source("https://www.vendor_org.com/MRF/v4.1/scripts/scale/scale.sh", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165")
        );
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldFetchSourcesFromBrokenFile() throws IOException {

        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplateWithBrokenSourcesSection.mf"));
        Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchSourcesSection();
        List<SourcesParser.Source> sources = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(sources).contains(
                new SourcesParser.Source("MRF.yaml", "SHA-256", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943"),
                new SourcesParser.Source("some_file.sh", "", ""),
                new SourcesParser.Source("scripts/install.sh", "", "d0e7828293355a07c2dccaaa765c80b507e60e6167067c950dc2e6b0da0dbd8b"),
                new SourcesParser.Source("https://www.vendor_org.com/MRF/v4.1/scripts/scale/scale.sh", "SHA-256", ""));
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldFetchNonManoArtifactsFromFile() {
        Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>> mapListPair = pnfManifestParser.fetchNonManoArtifacts().get();
        Map<String, Map<String, List<String>>> nonManoArtifacts = mapListPair.getKey();
        List<CSARArchive.CSARError> errors = mapListPair.getValue();

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
        assertThat(errors.size()).isEqualTo(0);
    }


    @Test
    public void shouldFetchCMS() {

        Pair<String, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchCMS();
        String cms = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(cms).isEqualTo(
                "MIGDBgsqhkiG9w0BCRABCaB0MHICAQAwDQYLKoZIhvcNAQkQAwgwXgYJKoZIhvcN" +
                        "AQcBoFEET3icc87PK0nNK9ENqSxItVIoSa0o0S/ISczMs1ZIzkgsKk4tsQ0N1nUM" +
                        "dvb05OXi5XLPLEtViMwvLVLwSE0sKlFIVHAqSk3MBkkBAJv0Fx0="
        );
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void shouldReportAnErrorWhenCMSSectionDoesNotHaveEndingMarker() throws IOException {
        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplateBrokenCMSNoEndMarker.mf"));
        Pair<String, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchCMS();
        String cms = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(cms).isEmpty();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0).getMessage()).isEqualTo("Invalid. Entry [Unable to find END CMS marker!]");
    }

    @Test
    public void shouldReturnEmptyCmsWhenBeginMarkerDoesNotExist() throws IOException {
        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplateBrokenCMSNoBeginMarker.mf"));
        Pair<String, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchCMS();
        String cms = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(cms).isEmpty();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0).getMessage()).isEqualTo("Invalid. Entry [Unable to find BEGIN CMS marker!]");
    }


    @Test
    public void shouldReportAnErrorWhenCMSIsNotAtTheEndOfFile() throws IOException {
        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplateCMSSectionNotAtTheEnd.mf"));
        Pair<String, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchCMS();

        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0).getMessage()).isEqualTo("Invalid. Entry [CMS section is not at the end of file!]");
    }

    @Test
    public void shouldFetchSourcesSectionFromFileWithChangedSectionOrder() throws IOException {

        pnfManifestParser = PnfManifestParser.getInstance(new File("./src/test/resources/pnf/MainServiceTemplateDifferentSectionOrder.mf"));
        Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchSourcesSection();
        List<SourcesParser.Source> sources = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(sources).contains(
                new SourcesParser.Source("MRF.yaml", "SHA-256", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943"),
                new SourcesParser.Source("scripts/install.sh", "SHA-256", "d0e7828293355a07c2dccaaa765c80b507e60e6167067c950dc2e6b0da0dbd8b"),
                new SourcesParser.Source("https://www.vendor_org.com/MRF/v4.1/scripts/scale/scale.sh", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Events/VES_registration.yml", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Measurements/PM_Dictionary.yaml", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Yang_module/Yang_module.yaml", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/scripts/install.sh", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Informational/user_guide.txt", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Other/installation_guide.txt", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165"),
                new SourcesParser.Source("Artifacts/Other/review_log.txt", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165")

        );
        assertThat(errors.size()).isEqualTo(0);
    }
}
