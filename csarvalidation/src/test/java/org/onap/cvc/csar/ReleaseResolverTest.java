/*
 * Copyright 2021 Nokia
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


import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReleaseResolverTest {

    private static final Properties APP_CONFIGURATION = new Properties();

    static {
        try {
            APP_CONFIGURATION.load(VTPValidateCSAR.class.getResourceAsStream("/vnfreqs.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ReleasesResolver releaseResolver = ReleasesResolver.create(APP_CONFIGURATION.getProperty("releases.order"));

    @Test
    public void shouldResolveReleasesForAmsterdam() {
        assertThat(releaseResolver.resolveWithAncestors("amsterdam"))
                .containsExactlyInAnyOrder("amsterdam");
    }

    @Test
    public void shouldResolveReleasesForCasablanca() {
        assertThat(releaseResolver.resolveWithAncestors("casablanca"))
                .containsExactlyInAnyOrder("amsterdam", "casablanca");

    }

    @Test
    public void shouldResolveReleasesForDublin() {
        assertThat(releaseResolver.resolveWithAncestors("dublin"))
                .containsExactlyInAnyOrder("amsterdam", "casablanca", "dublin");
    }

    @Test
    public void shouldResolveReleasesForGuilin() {
        assertThat(releaseResolver.resolveWithAncestors("guilin"))
                .containsExactlyInAnyOrder("amsterdam", "casablanca", "dublin", "frankfurt", "guilin");
    }

    @Test
    public void shouldResolveReleasesForHonolulu() {
        assertThat(releaseResolver.resolveWithAncestors("honolulu"))
                .containsExactlyInAnyOrder("amsterdam", "casablanca", "dublin", "frankfurt", "guilin", "honolulu");
    }

    @Test
    public void shouldResolveReleasesForLatest() {
        assertThat(releaseResolver.resolveWithAncestors("latest"))
                .containsExactlyInAnyOrder("amsterdam", "casablanca", "dublin", "frankfurt", "guilin", "honolulu");
    }

    @Test
    public void shouldReportAnErrorWhenReleaseIsUnknown() {
        assertThatThrownBy(() ->
                releaseResolver.resolveWithAncestors("unknown")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Release 'unknown' is not defined at the releases.order list in a vnfreqs.properties file!");
    }
}
