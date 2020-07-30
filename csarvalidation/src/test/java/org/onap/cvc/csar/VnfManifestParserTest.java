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

package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.cvc.csar.parser.SourcesParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VnfManifestParserTest {

    private VnfManifestParser vnfManifestParser;

    @Before
    public void setUp() throws IOException {
        vnfManifestParser = VnfManifestParser.getInstance(new File("./src/test/resources/vnf/MainServiceTemplate.mf"));
    }

    @Test
    public void shouldFetchMetadataFromFile() {
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> metadataListPair = vnfManifestParser.fetchMetadata();
        CSARArchive.Manifest.Metadata metadata = metadataListPair.getKey();
        List<CSARArchive.CSARError> errors = metadataListPair.getValue();

        assertThat(errors.size()).isZero();
        assertThat(metadata.getProductName()).isEqualTo(" vCSCF");
        assertThat(metadata.getProviderId()).isEqualTo(" ZTE");
        assertThat(metadata.getPackageVersion()).isEqualTo(" 1.0");
        assertThat(metadata.getReleaseDateTime()).isEqualTo(" 2017.01.01T10");
    }

    @Test
    public void shouldFetchSourcesSectionFromFile() {

        Pair<List<SourcesParser.Source>, List<CSARArchive.CSARError>> sourcesPair = vnfManifestParser.fetchSourcesSection();
        List<SourcesParser.Source> sources = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(sources).contains(
                new SourcesParser.Source("MainServiceTemplate.yaml", "", ""),
                new SourcesParser.Source("Definitions/openonfv__tosca.capabilities.nfv.ext.LocalAttachment.yaml", "", ""),
                new SourcesParser.Source("MRF.yaml", "SHA-256", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943"),
                new SourcesParser.Source("scripts/install.sh", "SHA-256", "d0e7828293355a07c2dccaaa765c80b507e60e6167067c950dc2e6b0da0dbd8b"),
                new SourcesParser.Source("https://www.vendor_org.com/MRF/v4.1/scripts/scale/scale.sh", "SHA-256", "36f945953929812aca2701b114b068c71bd8c95ceb3609711428c26325649165")
        );
        assertThat(errors.size()).isZero();
    }

    @Test
    public void shouldFetchCMS() {

        Pair<String, List<CSARArchive.CSARError>> sourcesPair = vnfManifestParser.fetchCMS();
        String cms = sourcesPair.getKey();
        List<CSARArchive.CSARError> errors = sourcesPair.getValue();

        assertThat(cms).isEqualTo(
                "MIGDBgsqhkiG9w0BCRABCaB0MHICAQAwDQYLKoZIhvcNAQkQAwgwXgYJKoZIhvcN" +
                        "AQcBoFEET3icc87PK0nNK9ENqSxItVIoSa0o0S/ISczMs1ZIzkgsKk4tsQ0N1nUM" +
                        "dvb05OXi5XLPLEtViMwvLVLwSE0sKlFIVHAqSk3MBkkBAJv0Fx0="
        );
        assertThat(errors.size()).isZero();
    }
}
