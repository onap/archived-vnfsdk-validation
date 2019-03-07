package org.onap.cvc.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PnfManifestParser {


    private final List<String> lines;

    private PnfManifestParser(List<String> lines) {
        this.lines = lines;
    }

    public static PnfManifestParser getInstance(String fileName) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream.collect(Collectors.toList());
            return new PnfManifestParser(lines);
        }


    }

    public CSARArchive.Manifest.Metadata fetchMetadata() {
        CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        boolean activate = false;
        for (String line : lines) {
            if (line.startsWith("metadata")) {
                activate = true;
                continue;
            }

            if (activate) {
                if (line.contains("pnf_product_name")) {
                    metadata.setProductName(fetchParameterValue(line));
                } else if (line.contains("pnf_provider_id")) {
                    metadata.setProviderId(fetchParameterValue(line));
                } else if (line.contains("pnf_package_version")) {
                    metadata.setPackageVersion(fetchParameterValue(line));
                } else if (line.contains("pnf_release_date_time")) {
                    metadata.setReleaseDateTime(fetchParameterValue(line));
                }
            }
        }

        return metadata;

    }

    public Map<String, Map<String, List<String>>> fetchNonManoArtifacts() {
        Map<String, Map<String, List<String>>> nonManoArtifacts = new HashMap<>();

        boolean activate = false;
        String attributeName = null;
        for (String line : lines) {
            if (line.startsWith("non_mano_artifact_sets")) {
                activate = true;
                continue;
            }

            if (activate) {
                if (line.contains("prv.onap.ves_event")) {
                    attributeName = "prv.onap.ves_event";
                } else if (line.contains("prv.onap.pm_dictionary")) {
                    attributeName = "prv.onap.pm_dictionary";
                } else if (line.contains("prv.onap.yang_modules")) {
                    attributeName = "prv.onap.yang_modules";
                } else {
                    storeNonManoArtifact(nonManoArtifacts, attributeName, line);
                }
            }
        }

        return nonManoArtifacts;
    }

    private void storeNonManoArtifact(Map<String, Map<String, List<String>>> nonManoArtifacts, String attributeName, String line) {
        final String value = fetchParameterValue(line);

        if (!nonManoArtifacts.containsKey(attributeName)) {
            Map<String, List<String>> attributeData = new HashMap<>();
            ArrayList<String> sourceValues = new ArrayList<>();
            attributeData.put("Source", sourceValues);
            sourceValues.add(value);
            nonManoArtifacts.put(attributeName, attributeData);
        } else {
            nonManoArtifacts.get(attributeName).get("Source").add(value);
        }
    }


    private String fetchParameterValue(String line) {
        String[] elements = line.split(": ");
        return elements[1].trim();
    }
}
