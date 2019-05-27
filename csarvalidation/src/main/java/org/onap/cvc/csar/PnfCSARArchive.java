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

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PnfCSARArchive extends CSARArchive {

    public PnfCSARArchive(){
        super(new PnfManifest());
    }

    @Override
    void parseManifest() throws IOException {
        PnfManifestParser pnfManifestParser = PnfManifestParser.getInstance(
                this.getManifestMfFile()
        );

        Pair<Manifest.Metadata, List<CSARError>> metadataData = pnfManifestParser.fetchMetadata();
        Pair<List<String>, List<CSARError>> sourcesSectionData = pnfManifestParser.fetchSourcesSection();
        Optional<Pair<Map<String, Map<String, List<String>>>, List<CSARError>>> nonManoArtifactsData = pnfManifestParser.fetchNonManoArtifacts();

        PnfManifest manifest = (PnfManifest) this.getManifest();
        manifest.setMetadata(metadataData.getKey());
        this.getErrors().addAll(metadataData.getValue());

        manifest.setSources(sourcesSectionData.getKey());
        this.getErrors().addAll(sourcesSectionData.getValue());

        if(nonManoArtifactsData.isPresent()){
            manifest.setNonMano(nonManoArtifactsData.get().getKey());
            this.getErrors().addAll(nonManoArtifactsData.get().getValue());
        }

    }

    @Override
    String getEntryManifestParamName() {
        return "ETSI-Entry-Manifest";
    }

    @Override
    String getEntryChangeLogParamName() {
        return "ETSI-Entry-Change-Log";
    }

    public static class PnfManifest extends Manifest {
        private List<String> sources = new ArrayList<>();

        public List<String> getSources() {
            return Collections.unmodifiableList(sources);
        }

        public void setSources(List<String> sources) {
            this.sources.addAll(sources);
        }
    }
}
