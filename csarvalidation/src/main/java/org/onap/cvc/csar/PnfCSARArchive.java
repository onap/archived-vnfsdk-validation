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

import org.apache.commons.lang3.tuple.Pair;

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

        PnfManifestParser pnfManifestParser = PnfManifestParser.getInstance(
                this.getManifestMfFile().getAbsolutePath()
        );

        Pair<Manifest.Metadata, List<CSARError>> metadataData = pnfManifestParser.fetchMetadata();
        Pair<Map<String, Map<String, List<String>>>, List<CSARError>> nonManoArtifactsData = pnfManifestParser.fetchNonManoArtifacts();

        Manifest manifest = this.getManifest();
        manifest.setMetadata(metadataData.getKey());
        manifest.setNonMano(nonManoArtifactsData.getKey());
        this.getErrors().addAll(metadataData.getValue());
        this.getErrors().addAll(nonManoArtifactsData.getValue());
    }

}
