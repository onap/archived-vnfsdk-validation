/*
  Copyright 2020 Nokia
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.parser.SourcesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsarSourcesSecurityValidator {

    private final Map<String, Map<String, List<String>>> nonMano;
    private final List<SourcesParser.Source> sources;
    private final Path csarRootDirectory;

    private List<CSARArchive.CSARError> errors;


    public CsarSourcesSecurityValidator(
        Map<String, Map<String, List<String>>> nonMano, List<SourcesParser.Source> sources, Path csarRootDirectory
    ) {
        this.nonMano = nonMano;
        this.sources = sources;
        this.csarRootDirectory = csarRootDirectory;
    }


    public List<CSARArchive.CSARError> validate() throws IOException, NoSuchAlgorithmException {
        errors = new ArrayList<>();
        validateSources(sources);
        validateNonManoCohesionWithSources(nonMano, sources);
        return errors;
    }

    public List<CSARArchive.CSARError> validate(Path commonCertificate) throws IOException, NoSuchAlgorithmException {
        errors = new ArrayList<>();
        validateSources(sources, commonCertificate);
        validateNonManoCohesionWithSources(nonMano, sources);
        return errors;
    }

    private void validateNonManoCohesionWithSources(final Map<String, Map<String, List<String>>> nonMano,
                                                    final List<SourcesParser.Source> sources) {

        final Collection<Map<String, List<String>>> values = nonMano.values();
        final List<String> nonManoSourcePaths = values.stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .flatMap(List::stream)
            .filter(it -> !it.isEmpty())
            .collect(Collectors.toList());

        final List<String> sourcePaths = sources.stream()
            .map(SourcesParser.Source::getValue)
            .collect(Collectors.toList());

        if (!sourcePaths.containsAll(nonManoSourcePaths)) {
            this.errors.add(new Error.CSARErrorContentMismatch());
        }

    }

    private void validateSources(List<SourcesParser.Source> sources)
        throws NoSuchAlgorithmException, IOException {
        for (SourcesParser.Source source : sources) {
            errors.addAll(new CsarSourceSecurityValidator(source, csarRootDirectory).validate());
        }
    }

    private void validateSources(List<SourcesParser.Source> sources, Path commonCertificate)
        throws NoSuchAlgorithmException, IOException {
        for (SourcesParser.Source source : sources) {
            errors.addAll(new CsarSourceSecurityValidator(source, csarRootDirectory).validate(commonCertificate));
        }
    }
}
