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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.onap.cvc.csar.parser.CmsParser;
import org.onap.cvc.csar.parser.MetadataParser;
import org.onap.cvc.csar.parser.NonManoArtifactsParser;
import org.onap.cvc.csar.parser.SourcesParser;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PnfMetadataParserTest {

    private static final String FILE_NAME = "fileName";

    @Test
    public void shouldReportAnErrorWhenMetadataSectionIsNotAvailable() {
        // given
        List<String> lines = Lists.newArrayList(
                "someSection:",
                "param_name: some value",
                "nextSection:",
                "param_name: some value"
        );


        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(
                lines,
                new MetadataParser(FILE_NAME),
                new SourcesParser(FILE_NAME),
                new NonManoArtifactsParser(),
                new CmsParser(FILE_NAME)
        );
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> data = pnfManifestParser.fetchMetadata();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Missing. Entry [metadata]"
        );
    }

    @Test
    public void shouldReportInvalidEntriesInMetadataSection() {

        // given
        List<String> lines = Lists.newArrayList(
                "metadata:",
                "pnfd_name: RadioNode",
                "pnfd_provider: Ericsson",
                "pnfd_archive_version: 1.0",
                "pnfd_release_date_time: 2019-01-14T11:25:00+00:00",
                "extra_param_1: some value",
                "extra_param_2: some value"
        );

        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(
                lines,
                new MetadataParser(FILE_NAME),
                new SourcesParser(FILE_NAME),
                new NonManoArtifactsParser(),
                new CmsParser(FILE_NAME)
        );
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> data = pnfManifestParser.fetchMetadata();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Invalid. Entry [extra_param_1]",
                "Invalid. Entry [extra_param_2]"
        );
    }


    @Test
    public void shouldParseMetadataToKnownSectionAndDoNotReportAnyError() {

        // given
        List<String> lines = Lists.newArrayList(
                "metadata:",
                "pnfd_name: RadioNode",
                "pnfd_provider: Ericsson",
                "pnfd_archive_version: 1.0",
                "# Ignore this comment",
                "pnfd_release_date_time: 2019-01-14T11:25:00+00:00",
                "non_mano_artifact_sets:",
                "param_name: some value"
        );

        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(
                lines,
                new MetadataParser(FILE_NAME),
                new SourcesParser(FILE_NAME),
                new NonManoArtifactsParser(),
                new CmsParser(FILE_NAME)
        );
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> data = pnfManifestParser.fetchMetadata();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(0);

    }



    @Test
    public void shouldReportAWarningWhenUnrecognizedSectionWasFoundDuringMetadataSectionParsing() {
        // given
        List<String> lines = Lists.newArrayList(
                "metadata:",
                "pnfd_name: RadioNode",
                "pnfd_provider: Ericsson",
                "pnfd_archive_version: 1.0",
                "pnfd_release_date_time: 2019-01-14T11:25:00+00:00",
                "unknown_section:",
                "param_name: some value"
        );

        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(
                lines,
                new MetadataParser(FILE_NAME),
                new SourcesParser(FILE_NAME),
                new NonManoArtifactsParser(),
                new CmsParser(FILE_NAME)
        );
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> data = pnfManifestParser.fetchMetadata();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Warning. Entry [unknown_section]"
        );
    }


    @Test
    public void shouldReportAInvalidWhenTokenWithOutColonWasFoundDuringMetadataProcessing() {
        // given
        List<String> lines = Lists.newArrayList(
                "metadata:",
                "pnfd_name RadioNode",
                "pnfd_provider: Ericsson",
                "pnfd_archive_version: 1.0",
                "pnfd_release_date_time: 2019-01-14T11:25:00+00:00"
        );

        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(
                lines,
                new MetadataParser(FILE_NAME),
                new SourcesParser(FILE_NAME),
                new NonManoArtifactsParser(),
                new CmsParser(FILE_NAME)
        );
        Pair<CSARArchive.Manifest.Metadata, List<CSARArchive.CSARError>> data = pnfManifestParser.fetchMetadata();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Invalid. Entry [pnfd_name RadioNode]"
        );
    }

}
