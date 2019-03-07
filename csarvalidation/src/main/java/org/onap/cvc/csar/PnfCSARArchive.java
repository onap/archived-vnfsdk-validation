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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PnfCSARArchive extends CSARArchive {

    @Override
    void parseManifest() throws IOException {

        //manifest is optional, so check for it
        if (this.getManifestMfFile() == null) {
            return;
        }

        Manifest manifest = this.getManifest();
        configureManifest(manifest,this.getManifestMfFile());
    }

    void configureManifest(Manifest manifest, File manifestMfFile) throws IOException {
        PnfManifestParser pnfManifestParser = PnfManifestParser.getInstance(manifestMfFile.getAbsolutePath());

        Manifest.Metadata metadata = pnfManifestParser.fetchMetadata();

        Map<String, Map<String, List<String>>> nonManoArtifacts = pnfManifestParser.fetchNonManoArtifacts();
        manifest.setMetadata(metadata);
        manifest.setNonMano(nonManoArtifacts);
    }
}
