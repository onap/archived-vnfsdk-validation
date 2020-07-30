/*
 * Copyright 2019 Nordix
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.PnfCSARError;
import org.onap.cvc.csar.PnfCSARError.PnfCSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.onap.cvc.csar.cc.sol004.VTPValidateCSARR972082.PnfSoftwareInformation.PnfSoftwareInformationField;
import org.onap.cvc.csar.cc.sol004.VTPValidateCSARR972082.PnfSoftwareInformation.PnfSoftwareVersion;
import org.onap.cvc.csar.cc.sol004.VTPValidateCSARR972082.PnfSoftwareInformation.PnfSoftwareVersion.PnfSoftwareVersionField;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

@OnapCommandSchema(schema = "vtp-validate-csar-r972082.yaml")
public class VTPValidateCSARR972082 extends VTPValidateCSARBase {

    private static final int UNKNOWN_LINE_NUMBER = -1;
    private static final String SOURCE_ELEMENT_TAG = "Source";
    private static final String ERROR_CODE = "0x2002";

    @Override
    protected void validateCSAR(final CSARArchive csar) {
        if (csar.getManifest().isNonManoAvailable()) {
            final Optional<ValidateNonManoSection> validateNonManoSection = ValidateNonManoSection.getInstance(csar);
            if (validateNonManoSection.isPresent()) {
                final List<CSARError> csarErrors = validateNonManoSection.get().validate();
                errors.addAll(csarErrors);
            }
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R972082";
    }

    private static class MissingSourceElementUnderAttributeError extends PnfCSARError {

        private MissingSourceElementUnderAttributeError(final String attributeName, final String fileName) {
            super(ERROR_CODE,
                String.format("Missing. Entry [%s under %s]", SOURCE_ELEMENT_TAG, attributeName),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class MissingSoftwareInformationError extends PnfCSARError {

        private MissingSoftwareInformationError(final String fileName) {
            super(ERROR_CODE,
                String.format("Missing. Entry [%s in %s]", "pnf_software_version", fileName),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class InvalidPathToFileError extends PnfCSARError {

        private InvalidPathToFileError(final String attributeName, final String pathToSourceFile, final String fileName) {
            super(ERROR_CODE,
                String.format("Invalid. Entry [%s under %s has invalid '%s' path]", SOURCE_ELEMENT_TAG, attributeName,
                    pathToSourceFile),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class InvalidYamlStructureError extends PnfCSARError {

        private InvalidYamlStructureError(final String fileName) {
            super(ERROR_CODE,
                String.format("Invalid. Yaml file %s is invalid", fileName),
                UNKNOWN_LINE_NUMBER,
                fileName);
        }
    }

    private static class InvalidFileExtensionError extends PnfCSARError {

        private InvalidFileExtensionError(final String fileName) {
            super(ERROR_CODE,
                    String.format("Invalid. File extension %s is invalid", fileName),
                    UNKNOWN_LINE_NUMBER,
                    fileName);
        }
    }

    private static class ValidateNonManoSection {

        private static final String ATTRIBUTE_NAME = "onap_pnf_sw_information";

        private final CSARArchive csar;
        private final String fileName;
        private final Map<String, Map<String, List<String>>> nonMano;
        private final List<CSARError> errors = new ArrayList<>();

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

        private List<CSARError> validate() {
            List<String> attributesNotNull = nonMano.keySet().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!attributesNotNull.isEmpty()) {
                attributesNotNull.forEach(this::validateAttribute);
            } else {
                errors.add(new PnfCSARErrorEntryMissing(ATTRIBUTE_NAME, fileName, UNKNOWN_LINE_NUMBER));
            }

            return errors;
        }

        private void validateAttribute(final String nonManoAttributes) {
            if (ATTRIBUTE_NAME.equals(nonManoAttributes)) {
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
                } else {
                    validateSoftwareInformationNonManoArtifact(pathToFile);
                }
            });
        }

        private void validateSoftwareInformationNonManoArtifact(final String swInformationFilePath) {
            if (StringUtils.isEmpty(swInformationFilePath)) {
                errors.add(new MissingSourceElementUnderAttributeError("", swInformationFilePath));
                return;
            } else if (!swInformationFilePath.matches(".*\\.yaml$")) {
                errors.add(new InvalidFileExtensionError(swInformationFilePath));
                return;
            }
            final Optional<PnfSoftwareInformation> parsedYaml = parse(swInformationFilePath);
            if (!parsedYaml.isPresent()) {
                errors.add(new InvalidYamlStructureError(swInformationFilePath));
            } else {
                final PnfSoftwareInformation pnfSoftwareInformation = parsedYaml.get();
                if (!pnfSoftwareInformation.isValid()) {
                    errors.add(new MissingSoftwareInformationError(swInformationFilePath));
                }
            }
        }

        private Object read(final InputStream yamlFileInputStream) {
            final Yaml yaml = new Yaml();
            return yaml.load(yamlFileInputStream);
        }

        private Optional<PnfSoftwareInformation> parse(final String swInformationFilePath) {

            final Map<String, Object> softwareVersionYamlObject;
            try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                Files.readAllBytes(csar.getFileFromCsar(swInformationFilePath).toPath()))) {
                final Object yaml = read(byteArrayInputStream);
                if (!(yaml instanceof Map)) {
                    return Optional.empty();
                }

                softwareVersionYamlObject = (Map<String, Object>) yaml; // unchecked warning suppressed
            } catch (final IOException | YAMLException e) {
                return Optional.empty();
            }

            final PnfSoftwareInformation pnfSoftwareInformation = new PnfSoftwareInformation();
            pnfSoftwareInformation.setDescription(
                (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.DESCRIPTION.getFieldName()));
            pnfSoftwareInformation.setProvider(
                (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.PROVIDER.getFieldName()));
            pnfSoftwareInformation.setVersion(
                (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.VERSION.getFieldName()));
            final List<Map<String, String>> pnfSoftwareInformationYaml = (List<Map<String, String>>) softwareVersionYamlObject
                .get(PnfSoftwareInformationField.PNF_SOFTWARE_INFORMATION
                    .getFieldName()); // unchecked warning suppressed

            if (CollectionUtils.isNotEmpty(pnfSoftwareInformationYaml)) {
                pnfSoftwareInformationYaml.forEach(stringStringMap -> {
                    final String description = stringStringMap.get(PnfSoftwareVersionField.DESCRIPTION.getFieldName());
                    final String version = stringStringMap
                        .get(PnfSoftwareVersionField.PNF_SOFTWARE_VERSION.getFieldName());
                    pnfSoftwareInformation.addToSoftwareVersionSet(new PnfSoftwareVersion(version, description));
                });
            }

            return Optional.of(pnfSoftwareInformation);
        }
    }

    @Getter
    @Setter
    static class PnfSoftwareInformation {

        private String description;
        private String provider;
        private String version;
        @Setter(AccessLevel.NONE)
        private Set<PnfSoftwareVersion> softwareVersionSet = new LinkedHashSet<>();

        /**
         * Adds a {@link PnfSoftwareVersion} instance to the software version set
         *
         * @param softwareVersion the pnf software version to add
         */
        private void addToSoftwareVersionSet(final PnfSoftwareVersion softwareVersion) {
            softwareVersionSet.add(softwareVersion);
        }

        /**
         * Stores the software information yaml field names.
         */
        @AllArgsConstructor
        @Getter
        enum PnfSoftwareInformationField {
            DESCRIPTION("description"),
            PROVIDER("provider"),
            VERSION("version"),
            PNF_SOFTWARE_INFORMATION("pnf_software_information");

            private final String fieldName;

        }

        private boolean isValid() {
            if (CollectionUtils.isEmpty(softwareVersionSet)) {
                return false;
            }

            return softwareVersionSet.stream().allMatch(PnfSoftwareVersion::isValid);
        }

        @AllArgsConstructor
        @EqualsAndHashCode
        @Getter
        static class PnfSoftwareVersion {

            private final String version;
            private final String description;

            /**
             * Stores the pnf software version yaml fields.
             */
            @Getter
            @AllArgsConstructor
            enum PnfSoftwareVersionField {
                DESCRIPTION("description"),
                PNF_SOFTWARE_VERSION("pnf_software_version");

                private final String fieldName;
            }

            private boolean isValid() {
                return StringUtils.isNotEmpty(version);
            }
        }
    }
}
