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

package org.onap.cvc.csar.cc.sol004;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.PnfCSARError;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;

@OnapCommandSchema(schema = "vtp-validate-csar-r146092.yaml")
public class VTPValidateCSARR146092 extends VTPValidateCSARBase {

    private static final int UNKNOWN_LINE_NUMBER = -1;
    private static final String SOURCE_ELEMENT_TAG = "Source";

    @Override
    protected void validateCSAR(final CSARArchive csar) {
        if (csar.getManifest().isNonManoAvailable()) {
            final Optional<ValidateNonManoSection> validateNonManoSection = ValidateNonManoSection.getInstance(csar);
            if (validateNonManoSection.isPresent()) {
                errors.addAll(validateNonManoSection.get().validate());
            }
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R146092";
    }

    private static class MissingSourceElementUnderAttributeError extends PnfCSARError {

        private MissingSourceElementUnderAttributeError(final String attributeName, final String fileName) {
            super("0x2002",
                String.format("Missing. Entry [%s under %s]", SOURCE_ELEMENT_TAG, attributeName),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class InvalidPathToFileError extends PnfCSARError {

        private InvalidPathToFileError(final String attributeName, final String pathToSourceFile, final String fileName) {
            super("0x2002",
                String.format("Invalid. Entry [%s under %s has invalid '%s' path]", SOURCE_ELEMENT_TAG, attributeName,
                    pathToSourceFile),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class ValidateNonManoSection {

        private final CSARArchive csar;
        private final String fileName;
        private final Map<String, Map<String, List<String>>> nonMano;
        private final List<CSARError> errors = new ArrayList<>();
        private final List<String> attributeNames = Arrays.asList(
            "onap_ansible_playbooks",
            "onap_others",
            "onap_pm_dictionary",
            "onap_pnf_sw_information",
            "onap_scripts",
            "onap_ves_events",
            "onap_yang_modules"
        );

        private ValidateNonManoSection(final CSARArchive csar, final String fileName,
                                       final Map<String, Map<String, List<String>>> nonMano) {
            this.csar = csar;
            this.fileName = fileName;
            this.nonMano = nonMano;
        }

        static Optional<ValidateNonManoSection> getInstance(final CSARArchive csar) {
            final File manifestMfFile = csar.getManifestMfFile();
            if (manifestMfFile == null) {
                return Optional.empty();
            }
            final String fileName = manifestMfFile.getName();
            final Map<String, Map<String, List<String>>> nonMano = csar.getManifest().getNonMano();
            return Optional.of(new ValidateNonManoSection(csar, fileName, nonMano));
        }

        public List<CSARError> validate() {
            if (nonMano.keySet().stream().filter(Objects::nonNull).count() > 0) {
                nonMano.keySet().stream().filter(Objects::nonNull).forEach(this::validateAttribute);
            } else {
                errors.add(new PnfCSARErrorEntryMissing(
                    attributeNames.toString(),
                    fileName,
                    UNKNOWN_LINE_NUMBER)
                );
            }

            return errors;
        }

        private void validateAttribute(final String nonManoAttributes) {

            if (!attributeNames.contains(nonManoAttributes)) {
                errors.add(new PnfCSARErrorEntryMissing(
                    nonManoAttributes,
                    fileName,
                    UNKNOWN_LINE_NUMBER)
                );
            } else {
                validateSourceElementsUnderAttribute(nonManoAttributes);
            }
        }

        private void validateSourceElementsUnderAttribute(final String attributeName) {

            final Map<String, List<String>> attributeElements = nonMano.get(attributeName);
            final Set<String> attributeElementNames = attributeElements.keySet();

            if (!attributeElementNames.contains(SOURCE_ELEMENT_TAG)) {
                errors.add(new MissingSourceElementUnderAttributeError(attributeName, fileName));
            } else {
                validateThatSourceFileExists(attributeName, attributeElements);
            }
        }

        private void validateThatSourceFileExists(final String attributeName, final Map<String, List<String>> attributeElements) {
            attributeElements.get(SOURCE_ELEMENT_TAG).forEach(pathToFile -> {
                final File fileFromCsar = csar.getFileFromCsar(pathToFile);
                if (!fileFromCsar.exists()) {
                    errors.add(new InvalidPathToFileError(attributeName, pathToFile, fileName));
                }
            });
        }
    }

}
