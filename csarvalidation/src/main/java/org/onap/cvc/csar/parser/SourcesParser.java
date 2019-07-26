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

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.PnfCSARError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.onap.cvc.csar.parser.ManifestConsts.*;

public class SourcesParser {

    private final String fileName;

    public SourcesParser(String fileName) {
        this.fileName = fileName;
    }

    public Pair<List<Source>, List<CSARArchive.CSARError>> parse(List<String> lines) {
        List<Source> sources = new ArrayList<>();
        List<CSARArchive.CSARError> errors = new ArrayList<>();
        boolean isSpecialTagReached = false;
        boolean sourceSectionParsing = false;
        Source source = null;

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            ManifestLine manifestLine = ManifestLine.of(line);
            if (sourceSectionParsing && isContainSpecialTag(line, manifestLine)) {
                isSpecialTagReached = true;
            } else if (!isSpecialTagReached && manifestLine.startsWith(SOURCE_TAG_SECTION)) {
                sourceSectionParsing = true;
                source = handleSourceLine(sources, errors, lineNumber, manifestLine);
            } else if (!isSpecialTagReached && manifestLine.startsWith(ALGORITHM)) {
                handleAlgorithmLine(errors, source, lineNumber, manifestLine);
            } else if (!isSpecialTagReached && manifestLine.startsWith(HASH)) {
                handleHashLine(errors, source, lineNumber, manifestLine);
            }
        }

        return Pair.of(sources, errors);
    }

    private boolean isContainSpecialTag(String line, ManifestLine manifestLine) {
        return manifestLine.startsWith(METADATA_SECTION_TAG_SECTION)
                || manifestLine.startsWith(NON_MANO_ARTIFACT_SETS_TAG_SECTION)
                || line.contains(CMS);
    }

    private Source handleSourceLine(List<Source> sources, List<CSARArchive.CSARError> errors, int lineNumber, ManifestLine manifestLine) {
        Source source;
        String value = parseSourceSectionLine(manifestLine, lineNumber, errors);
        if (!value.isEmpty()) {
            source = new Source(value);
            sources.add(source);
        } else {
            source = null;
        }
        return source;
    }

    private void handleAlgorithmLine(List<CSARArchive.CSARError> errors, Source source, int lineNumber, ManifestLine manifestLine) {
        String algorithm = parseSourceSectionLine(manifestLine, lineNumber, errors);
        if (source != null)
            source.setAlgorithm(algorithm);
    }

    private void handleHashLine(List<CSARArchive.CSARError> errors, Source source, int lineNumber, ManifestLine manifestLine) {
        String hash = parseSourceSectionLine(manifestLine, lineNumber, errors);
        if (source != null)
            source.setHash(hash);
    }

    private String parseSourceSectionLine(ManifestLine line, int lineNumber, List<CSARArchive.CSARError> errors) {
        String retVal = "";
        Pair<String, String> data = line.parse();

        String value = data.getValue();
        if (value.isEmpty()) {
            errors.add(new PnfCSARError.PnfCSARErrorWarning(data.getKey(), this.fileName, lineNumber));
        } else {
            retVal = value;
        }

        return retVal;
    }

    public static class Source {


        private final String value;
        private String algorithm;
        private String hash;

        public Source(String value, String algorithm, String hash) {

            this.value = value;
            this.algorithm = algorithm;
            this.hash = hash;
        }

        public Source(String source) {
            this(source, "", "");
        }

        public String getValue() {
            return value;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Source source1 = (Source) o;
            return Objects.equals(value, source1.value) &&
                    Objects.equals(algorithm, source1.algorithm) &&
                    Objects.equals(hash, source1.hash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, algorithm, hash);
        }

        @Override
        public String toString() {
            return "Source{" +
                    "value='" + value + '\'' +
                    ", algorithm='" + algorithm + '\'' +
                    ", hash='" + hash + '\'' +
                    '}';
        }
    }
}
