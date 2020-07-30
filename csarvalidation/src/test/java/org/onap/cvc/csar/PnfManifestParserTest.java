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
import java.util.Collections;
import java.util.HashMap;

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
        assertThat(errors.size()).isZero();
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
        assertThat(errors.size()).isZero();
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
        assertThat(errors.size()).isZero();
    }


    @Test
    public void shouldFetchNonManoArtifactsFromFile() {
        Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>> mapListPair = pnfManifestParser.fetchNonManoArtifacts().get();
        Map<String, Map<String, List<String>>> nonManoArtifacts = mapListPair.getKey();
        List<CSARArchive.CSARError> errors = mapListPair.getValue();
        Map<String, List<String>>mapValue1=new HashMap<>();
        Map<String, List<String>>mapValue2=new HashMap<>();
        Map<String, List<String>>mapValue3=new HashMap<>();
        Map<String, List<String>>mapValue4=new HashMap<>();

        mapValue1.put("", Collections.singletonList(""));
        mapValue1.put("source",Lists.newArrayList("Artifacts/Events/VES_registration.yml"));
        mapValue2.put("", Collections.singletonList(""));
        mapValue2.put("source",Lists.newArrayList("Artifacts/Measurements/PM_Dictionary.yaml"));
        mapValue3.put("", Collections.singletonList(""));
        mapValue3.put("source",Lists.newArrayList("Artifacts/Yang_module/Yang_module.yaml"));
        mapValue4.put("", Collections.singletonList(""));
        mapValue4.put("source",Lists.newArrayList
                ("Artifacts/scripts/install.sh",
                        "Artifacts/Informational/user_guide.txt",
                        "Artifacts/Other/installation_guide.txt",
                        "Artifacts/Other/review_log.txt"));
        assertThat(nonManoArtifacts).containsEntry("onap_ves_events",mapValue1);
        assertThat(nonManoArtifacts).containsEntry("onap_pm_dictionary",mapValue2);
        assertThat(nonManoArtifacts).containsEntry("onap_yang_module",mapValue3);
        assertThat(nonManoArtifacts).containsEntry("onap_others",mapValue4);
        assertThat(errors.size()).isZero();
    }


    @Test
    public void shouldFetchCMS() {

        Pair<String, List<CSARArchive.CSARError>> sourcesPair = pnfManifestParser.fetchCMS();
        String cms = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(cms).isEqualTo(
                "MIIGDAYJKoZIhvcNAQcCoIIF/TCCBfkCAQExDTALBglghkgBZQMEAgEwCwYJKoZI"+
                "hvcNAQcBoIIDRTCCA0EwggIpAhRJ6KO7OFR2BuRDZwcd2TT4/wrEqDANBgkqhkiG"+
                "9w0BAQsFADBlMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8G"+
                "A1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMR4wHAYDVQQLDBVDZXJ0aWZp"+
                "Y2F0ZSBBdXRob3JpdHkwHhcNMTkwODEzMDg0NDU4WhcNMTkwOTEyMDg0NDU4WjBV"+
                "MQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50"+
                "ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMQ4wDAYDVQQLDAVOb2tpYTCCASIwDQYJKoZI"+
                "hvcNAQEBBQADggEPADCCAQoCggEBAPRCM8g0cm1nuojFuk01Lo1iAbj7STEbiqJn"+
                "Xk4BEEspM4snShj35bO9DHSunXivdCzen4BE7hLpetpbr+7ptqpV7NuR9DgYD399"+
                "eAltb4oLnkLWgODCxhFOnwrKjnXSbP8KX3kmYRJmDzsSjJrpattfxNCa2aHzubyA"+
                "W0Mv9Ni2R0scnBY+ubydwn223d/743T2pfXsiOV6Ucjhz+9XWU96b7e9GxN12EJQ"+
                "R6R4O9dz3CSZmQsiMMYROD5elV59Y9ucSkhdrUjPzjveqjEA9FWc0piBpe42c9Mo"+
                "Lr8S5hKaaC8ONfSUBuEysKC5g6D6OS1Kxii3zbUbNzpxXti8tmUCAwEAATANBgkq"+
                "hkiG9w0BAQsFAAOCAQEAVJGCH8VL/ha1RYmoZBefCT/AQc50GlcIJtPCB8Y7ygkX"+
                "Y2Ybj6SrF66+wq6hQsU9xtxHyn08nfOdGWfNJ9yq4SO8RF7Oz4NxkQ+KFhi2QUGZ"+
                "5TwdWLr0Q+zKTZgpLZm1rtlyyz+2AUwcPPVHhDfJX0kqz/0UPHWFDxXfJyOwmQdN"+
                "E4qhO9uB3zEujJwM8B7wXfDwsNg6xbKBytm67IHQN3OF/Bfcugx7eCVJ08XA8Irj"+
                "CovwPvjxaL32iYTXmiBl+vSb3lEarbinMkMCq80yx3LtIg1goGVO+Tp+yOoVxNUL"+
                "psSXr9kdWncI1venEjk/SvggxtT4RJ6dLH358qFu+TGCAo0wggKJAgEBMH0wZTEL"+
                "MAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVy"+
                "bmV0IFdpZGdpdHMgUHR5IEx0ZDEeMBwGA1UECwwVQ2VydGlmaWNhdGUgQXV0aG9y"+
                "aXR5AhRJ6KO7OFR2BuRDZwcd2TT4/wrEqDALBglghkgBZQMEAgGggeQwGAYJKoZI"+
                "hvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMTkwODEzMDg0NTI2"+
                "WjAvBgkqhkiG9w0BCQQxIgQgOtAYNJkSFj5rU8K7Ujz6BdefH7sITKBcMmBcm/hI"+
                "TUAweQYJKoZIhvcNAQkPMWwwajALBglghkgBZQMEASowCwYJYIZIAWUDBAEWMAsG"+
                "CWCGSAFlAwQBAjAKBggqhkiG9w0DBzAOBggqhkiG9w0DAgICAIAwDQYIKoZIhvcN"+
                "AwICAUAwBwYFKw4DAgcwDQYIKoZIhvcNAwICASgwDQYJKoZIhvcNAQEBBQAEggEA"+
                "Sj+3i3Mcxz6Uqf8WcLNiR3K3QeQUEQJurPHW/BzidjPx+PoZ+6jP8sAkulUu/yeo"+
                "rv3dDQGq0cF6KE3gKi3IXgCOB5nZ/O4BtvPcKOlQk14fMdBnHQMgGb27dNLMheuo"+
                "t4YJVEZNm+1NoYZBMyESm1Ns3DHmq7dqpFMWSad85gMTsbD/q896ZMiua+bLvnlg"+
                "qJXtYrnJPx9KqSzNFhzTqwFMJ9OASaHm+eV9/EWWLJ0rgUmheI0sb2Pa5i93w6dr"+
                "HhE7UbSCHDlDDgrOosJkbuI4UCX/njXrU2ukXbrWz/FjH84Mek039z+w4M6fBnl5"+
                "4xuyO1o65LlKHoxwnRH9lQ=="
        );
        assertThat(errors.size()).isZero();
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
        assertThat(errors.size()).isZero();
    }
}
