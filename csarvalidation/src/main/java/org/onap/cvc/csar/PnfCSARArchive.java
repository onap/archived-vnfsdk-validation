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
