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

package org.onap.cvc.csar.parser;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;

/*
 How to sing files see to README.txt file into test/resources folder
 */
public class ManifestFileSplitterTest {

    private ManifestFileModel getModel(String resourceFilePath) throws URISyntaxException {
        File file = new File(absoluteFilePath(resourceFilePath));
        ManifestFileSplitter manifestFileSplitter = new ManifestFileSplitter();
        return manifestFileSplitter.split(file);
    }

    @Test
    public void shouldSplitManifestFileOnDataPartAndCMS() throws URISyntaxException {
        String resource = "cvc/csar/parser/MainServiceTemplate.mf";
        ManifestFileModel manifestFileModel = getModel(resource);

        Assertions.assertThat(manifestFileModel.getNewLine()).isEqualTo(System.lineSeparator());
        Assertions.assertThat(manifestFileModel.getData()).contains("metadata:",
                "    pnfd_name: RadioNode",
                "    pnfd_provider: Ericsson",
                "    pnfd_archive_version: 1.0",
                "    pnfd_release_date_time: 2019-01-14T11:25:00+00:00");

        Assertions.assertThat(manifestFileModel.getCMS()).contains(
                "-----BEGIN CMS-----",
                "MIIGDAYJKoZIhvcNAQcCoIIF/TCCBfkCAQExDTALBglghkgBZQMEAgEwCwYJKoZI",
                "hvcNAQcBoIIDRTCCA0EwggIpAhRJ6KO7OFR2BuRDZwcd2TT4/wrEqDANBgkqhkiG",
                "-----END CMS-----"
        );
    }

    @Test
    public void shouldBeWindowsStyle() throws URISyntaxException {
        String resource = "cvc/csar/parser/MainServiceTemplate.windows.mf";
        ManifestFileModel manifestFileModel = getModel(resource);
        Assertions.assertThat(manifestFileModel.getNewLine()).isEqualTo("\r\n");
    }
}
