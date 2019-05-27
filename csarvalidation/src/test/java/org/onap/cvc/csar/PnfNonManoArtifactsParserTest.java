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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PnfNonManoArtifactsParserTest {

    @Test
    public void shouldDoNotReportAnErrorWhenNonManoArtifactSectionIsNotAvailable() {
        // given
        List<String> lines = Lists.newArrayList(
                "someSection:",
                "param_name: some value",
                "nextSection:",
                "param_name: some value"
        );


        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(lines, "fileName");
        Optional<Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>>> nonManoArtifacts =
                pnfManifestParser.fetchNonManoArtifacts();

        //then
        assertThat(nonManoArtifacts.isPresent()).isFalse();
    }

    @Test
    public void shouldParseNonManoArtifactsInProperFormatAndDoNotReportAnyError() {

        // given
        List<String> lines = Lists.newArrayList(
                "non_mano_artifact_sets:",
                                "# Ignore this comment",
                                "onap_ves_events:  # if private else onap_ves_event if public",
                                    "source: Artifacts/Events/VES_registration.yml",
                                    "extra_param_1: some value",
                                    "extra_param_2: some value",
                                "onap_pm_dictionary:    # if private else onap_pm_dictionary if public",
                                    "source: Artifacts/Measurements/PM_Dictionary.yaml",
                                "param name:     # if private else onap_yang_modules if public",
                                    "key: value"
        );

        // when
        PnfManifestParser pnfManifestParser = new PnfManifestParser(lines, "fileName");
        Pair<Map<String, Map<String, List<String>>>, List<CSARArchive.CSARError>>  data = pnfManifestParser.fetchNonManoArtifacts().get();

        //then
        List<CSARArchive.CSARError> errors = data.getRight();
        assertThat(errors.size()).isEqualTo(0);

    }
}
