/**
 * Copyright 2019 Huawei Technologies Co., Ltd.
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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.parser.SourcesParser;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verify the CSAR package by following the SOL004 specifications and ONAP VNFREQS for TOSCA.
 *
 * @author Kanagaraj Manickam kanagaraj.manickam@huawei.com
 *
 */
public class CSARArchive implements AutoCloseable {

    public static final String SOL0004_2_4_1 = "V2.4.1 (2018-02)";

    public static final String TEMP_DIR = "/tmp";

    public static final String TOSCA_Metadata = "TOSCA-Metadata";

    public static final String TOSCA_Metadata__TOSCA_Meta = "TOSCA.meta";

    public static final String TOSCA_Metadata__TOSCA_Meta__TOSCA_Meta_File_Version = "TOSCA-Meta-File-Version";

    public static final String TOSCA_Metadata__TOSCA_Meta__CSAR_Version = "CSAR-Version";

    public static final String TOSCA_Metadata__TOSCA_Meta__Created_by = "Created-by";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Definitions = "Entry-Definitions";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Manifest = "Entry-Manifest";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Change_Log = "Entry-Change-Log";

    public static final String Change_Logs_Txt = "Change-Logs.txt";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Tests = "Entry-Tests";

    public static final String Tests = "Tests";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Licenses = "Entry-Licenses";

    public static final String Licenses = "Licenses";

    public static final String TOSCA_Metadata__TOSCA_Meta__Entry_Certificate = "Entry-Certificate";

    public static final String Certificate = "Certificate";

    public static final String TOSCA_Metadata__TOSCA_Meta__Name = "Name";

    public static final String TOSCA_Metadata__TOSCA_Meta__Content_Type = "Content-Type";

    public static final String Entry_Definition__tosca_definitions_version = "tosca_definitions_version";

    public static final String Entry_Definition__tosca_definitions_version__simple_1_0 = "tosca_simple_yaml_1_0";
    public static final String Entry_Definition__tosca_definitions_version__simple_1_1 = "tosca_simple_yaml_1_1";
    public static final String Entry_Definition__tosca_definitions_version__simple_1_2 = "tosca_simple_yaml_1_2";

    protected static final String[] Entry_Definition__tosca_definitions_versions = new String[] {
            Entry_Definition__tosca_definitions_version__simple_1_0,
            Entry_Definition__tosca_definitions_version__simple_1_1,
            Entry_Definition__tosca_definitions_version__simple_1_2
    };
    public static final String Entry_Definition__metadata = "metadata";

    public static final String Entry_Definition__template_name = "template_name";

    public static final String Entry_Definition__template_author = "template_author";

    public static final String Entry_Definition__template_version = "template_version";

    public static final String CSAR_Archive = "CSAR Archive";

    public enum Mode {
        WITH_TOSCA_META_DIR,
        WITHOUT_TOSCA_META_DIR
    }

    public static class CSARError{

        public CSARError(String code) {
            this.code = code;
        }

        private String vnfreqNo;

        private String code;

        protected String message = "";

        protected String file = null;

        protected int lineNumber = -1;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getVnfreqNo() {
            return this.vnfreqNo;
        }

        public void setVnfreqNo(String no) {
            this.vnfreqNo = no;
        }

        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                //never occurs
                return "{}";
            }
        }
    }

    public static class CSARErrorInvalidEntry extends CSARError {
        public CSARErrorInvalidEntry(String entry, String file, int lineNo, String message) {
            super("0x1000");
            this.message = "Invalid. Entry [" + entry + "]";
            if (message != null) {
                this.message += ". " + message;
            }
            this.file = file;
            this.lineNumber = lineNo;
        }
    }

    public static class CSARErrorInvalidEntryValue extends CSARError {
        public CSARErrorInvalidEntryValue(String entry, String file, int lineNo, String message, String validValues) {
            this(entry, file, message, validValues);
            this.lineNumber = lineNo;
        }

        public CSARErrorInvalidEntryValue(String entry, String file, String message, String validValues) {
            super("0x1001");
            this.message = "Invalid value. Entry [" + entry + "]";
            if (validValues != null) {
                this.message += ". Valid values are [" + validValues + "]";
            }

            if (message != null) {
                this.message += ". " + message;
            }

            this.file = file;
        }
    }

    public static class CSARErrorEntryMissing extends CSARError {
        public CSARErrorEntryMissing(String entry, String file, int lineNo, String message) {
            super("0x1002");
            this.message = "Missing. Entry [" + entry + "]";
            if (message != null) {
                this.message += ". " + message;
            }
            this.file = file;
            this.lineNumber = lineNo;
        }

        public CSARErrorEntryMissing(String entry, String file) {
            super("0x1002");
            this.message = "Missing. Entry [" + entry + "]";
            this.file = file;
        }
    }


    public static class CSARErrorConflicts extends CSARError {
        public CSARErrorConflicts(String entry, String file, int lineNo, String message, String conflicts) {
            super("0x1003");
            this.message = "Conflicts. Entry [" + entry + "]";
            if (conflicts != null) {
                this.message += ".  Conflicting entries [" + conflicts + "]";
            }
            if (message != null) {
                this.message += ". " + message;
            }

            this.lineNumber = lineNo;
            this.file = file;
        }
    }

    public static class CSARErrorMismatch extends CSARError {
        public CSARErrorMismatch(String entry, String file, int lineNo, String message, String expected, String acutal) {
            super("0x1004");
            this.message = "Mismatch. Entry [" + entry + "]. expected: [" + expected + "] actual: ["+ acutal + "]";
            if (message != null) {
                this.message += ". " + message;
            }
            this.file = file;
            this.lineNumber = lineNo;
        }
    }

    public static class CSARErrorIgnored extends CSARError {
        public CSARErrorIgnored(String entry, String file, int lineNo, String message) {
            super("0x1005");
            this.message = "Ignored. Entry [" + entry + "]";
            if (message != null) {
                this.message += ". " + message;
            }

            this.file = file;
            this.lineNumber = lineNo;
        }
    }

    public static class CSARErrorWarning extends CSARError {
        public CSARErrorWarning(String entry, String file, int lineNo, String message) {
            super("0x1006");
            this.file = file;
            this.message = "Warning. Entry [" + entry + "]";
            if (message != null) {
                this.message += ". " + message;
            }

            this.lineNumber = lineNo;
        }
    }

    public static class CSARErrorUnknown extends CSARError {

        public CSARErrorUnknown(String message) {
            super("0x1007");
            this.message = message;
        }
    }


    //Specific errors
    public static class CSARErrorEntryMissingToscaDefinitionVersion extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaDefinitionVersion(String definitionYaml) {
            super(Entry_Definition__tosca_definitions_version,
                    definitionYaml,
                    -1,
                    null);
            this.setCode("0x001");
        }
    }

    public static class CSARErrorInvalidEntryValueToscaDefinitionVersion extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueToscaDefinitionVersion(String definitionYaml) {
            super(Entry_Definition__tosca_definitions_version,
                    definitionYaml,
                    -1,
                    null,
                    Entry_Definition__tosca_definitions_version__simple_1_1);

            this.setCode("0x0002");
        }
    }

    //In non TOSCA-Meta mode, this is mandatory
    public static class CSARErrorEntryMissingToscaDefinitionMetadataTemplateAuthor extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaDefinitionMetadataTemplateAuthor(String definitionYaml) {
            super(Entry_Definition__template_author,
                    definitionYaml,
                    -1,
                    null);

            this.setCode("0x0003");
        }
    }

    //In non TOSCA-Meta mode, this is mandatory
    public static class CSARErrorEntryMissingToscaDefinitionMetadataTemplateName extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaDefinitionMetadataTemplateName(String definitionYaml) {
            super(Entry_Definition__template_name,
                    definitionYaml,
                    -1,
                    null);

            this.setCode("0x0004");
        }
    }

    //In non TOSCA-Meta mode, this is mandatory
    public static class CSARErrorEntryMissingToscaDefinitionMetadataTemplateVersion extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaDefinitionMetadataTemplateVersion(String definitionYaml) {
            super(Entry_Definition__template_version,
                    definitionYaml,
                    -1,
                    null);

            this.setCode("0x0005");
        }
    }

    public static class CSARErrorInvalidEntryValueToscaDefinitionNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueToscaDefinitionNotFound(String definitionYaml, int lineNo) {
            super(TOSCA_Metadata__TOSCA_Meta__Entry_Definitions,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    definitionYaml + " does not exist",
                    null);

            this.setCode("0x0006");
        }
    }

    public static class CSARErrorInvalidEntryValueManifestNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueManifestNotFound(String manifest, int lineNo, String entryManifestArgumentName) {
            super(entryManifestArgumentName,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    manifest + " does not exist",
                    null);

            this.setCode("0x0007");
        }
    }

    public static class CSARErrorInvalidEntryValueLogsNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueLogsNotFound(String logs, int lineNo, String entryChangeLogArgumentName) {
            super(entryChangeLogArgumentName,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    logs + " does not exist",
                    null);

            this.setCode("0x0008");
        }
    }

    public static class CSARErrorInvalidEntryValueTestsNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueTestsNotFound(String tests, int lineNo) {
            super(TOSCA_Metadata__TOSCA_Meta__Entry_Tests,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    tests + " folder does not exist",
                    null);

            this.setCode("0x0009");
        }
    }

    public static class CSARErrorInvalidEntryValueLicenseNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueLicenseNotFound(String license, int lineNo) {
            super(TOSCA_Metadata__TOSCA_Meta__Entry_Licenses,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    license + " does not exist",
                    null);

            this.setCode("0x000a");
        }
    }

    public static class CSARErrorInvalidEntryValueCertificatesNotFound extends CSARErrorInvalidEntryValue {
        public CSARErrorInvalidEntryValueCertificatesNotFound(String certificate, int lineNo) {
            super(TOSCA_Metadata__TOSCA_Meta__Entry_Certificate,
                    TOSCA_Metadata__TOSCA_Meta,
                    lineNo,
                    certificate + " does not exist",
                    null);

            this.setCode("0x000b");
        }
    }

    public static class CSARErrorEntryMissingToscaMetaFileVersion extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaMetaFileVersion() {
            super(TOSCA_Metadata__TOSCA_Meta__TOSCA_Meta_File_Version,
                    TOSCA_Metadata__TOSCA_Meta,
                    -1,
                    null);

            this.setCode("0x000c");
        }
    }

    public static class CSARErrorEntryMissingToscaMetaDefinition extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaMetaDefinition() {
            super(TOSCA_Metadata__TOSCA_Meta__Entry_Definitions,
                    TOSCA_Metadata__TOSCA_Meta,
                    -1,
                    null);

            this.setCode("0x000d");
        }
    }

    public static class CSARErrorEntryMissingToscaMetaCSARVersion extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaMetaCSARVersion() {
            super(TOSCA_Metadata__TOSCA_Meta__CSAR_Version,
                    TOSCA_Metadata__TOSCA_Meta,
                    -1,
                    null);

            this.setCode("0x000e");
        }
    }

    public static class CSARErrorEntryMissingToscaMetaCreatedBy extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaMetaCreatedBy() {
            super(TOSCA_Metadata__TOSCA_Meta__Created_by,
                    TOSCA_Metadata__TOSCA_Meta,
                    -1,
                    null);

            this.setCode("0x000f");
        }
    }

    public static class CSARErrorEntryMissingToscaDefinitionNotFound extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingToscaDefinitionNotFound() {
            super("Definition YAML",
                    CSAR_Archive,
                    -1,
                    null);

            this.setCode("0x0010");
        }
    }

    public static class CSARErrorConflictsMultipleDefinitionYamls extends CSARErrorConflicts {
        public CSARErrorConflictsMultipleDefinitionYamls(String fileNames) {
            super("Definition YAML",
                    CSAR_Archive,
                    -1,
                    "Only one definition YAML should be provided at the root of the archive",
                    fileNames);

            this.setCode("0x0011");
        }
    }


    public static class CSARErrorConflictsMultipleManifests extends CSARErrorConflicts {
        public CSARErrorConflictsMultipleManifests(String fileNames) {
            super("Manifest MF",
                    CSAR_Archive,
                    -1,
                    "Only one manifest MF file should be provided at the root of the archive",
                    fileNames);

            this.setCode("0x0012");
        }
    }

    public static class CSARErrorMismatchDefinitionYamlVsManifestMf extends CSARErrorMismatch {
        public CSARErrorMismatchDefinitionYamlVsManifestMf(String definitionYaml, String manifest) {
            super("Manifest MF",
                    CSAR_Archive,
                    -1,
                    "Manifest file name should match the definition YAML name",
                    definitionYaml + ".mf", //fix the name part
                    manifest);

            this.setCode("0x0013");
        }
    }

    public static class CSARErrorConflictsMultipleCertificates extends CSARErrorConflicts {
        public CSARErrorConflictsMultipleCertificates(String fileNames) {
            super("Certificate CERT",
                    CSAR_Archive,
                    -1,
                    "Only one certificates file should be provided at the root of the archive",
                    fileNames);

            this.setCode("0x0014");
        }
    }

    public static class CSARErrorMismatchDefinitionYamlVsCertificateCert extends CSARErrorMismatch {
        public CSARErrorMismatchDefinitionYamlVsCertificateCert(String definitionYaml, String certificate) {
            super("Certificate CERT",
                    CSAR_Archive,
                    -1,
                    "certificate file name should match the definition YAML name",
                    definitionYaml + ".cert", //fix the name part
                    certificate);

            this.setCode("0x0015");
        }
    }

    /**
     * Holds the CSAR meta data values in both Modes
     *
     */
    public static class TOSCAMeta {
        private Mode mode;

        private String metaDataFileVersion;

        private String csarVersion;

        private String companyName;

        private String entryDefinitionYaml;

        private String entryManifestMf;

        private String entryChangeLog;

        private String entryTest;

        private String entryLicense;

        private String entryCertificate;

        public String getEntryCertificate() {
            return entryCertificate;
        }


        public void setEntryCertificate(String entryCertificate) {
            this.entryCertificate = entryCertificate;
        }


        public Mode getMode() {
            return mode;
        }


        public void setMode(Mode mode) {
            this.mode = mode;
        }


        public String getMetaDataFileVersion() {
            return metaDataFileVersion;
        }


        public void setMetaDataFileVersion(String metaDataFileVersion) {
            this.metaDataFileVersion = metaDataFileVersion;
        }


        public String getCsarVersion() {
            return csarVersion;
        }


        public void setCsarVersion(String csarVersion) {
            this.csarVersion = csarVersion;
        }


        public String getCompanyName() {
            return companyName;
        }


        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }


        public String getEntryDefinitionYaml() {
            return entryDefinitionYaml;
        }


        public void setEntryDefinitionYaml(String entryDefinitionYaml) {
            this.entryDefinitionYaml = entryDefinitionYaml;
        }


        public String getEntryManifestMf() {
            return entryManifestMf;
        }


        public void setEntryManifestMf(String entryManifestMf) {
            this.entryManifestMf = entryManifestMf;
        }


        public String getEntryChangeLog() {
            return entryChangeLog;
        }


        public void setEntryChangeLog(String entryChangeLog) {
            this.entryChangeLog = entryChangeLog;
        }


        public String getEntryTest() {
            return entryTest;
        }


        public void setEntryTest(String entryTest) {
            this.entryTest = entryTest;
        }


        public String getEntryLicense() {
            return entryLicense;
        }


        public void setEntryLicense(String entryLicense) {
            this.entryLicense = entryLicense;
        }
    }

    public static class Definition {
        private String toscaDefinitionVersion;

        public String getToscaDefinitionVersion() {
            return toscaDefinitionVersion;
        }

        public void setToscaDefinitionVersion(String toscaDefinitionVersion) {
            this.toscaDefinitionVersion = toscaDefinitionVersion;
        }

        public static class Metadata {
            private String tempalteName;

            private String templateAuthor;

            private String templateVersion;

            public String getTempalteName() {
                return tempalteName;
            }

            public void setTempalteName(String tempalteName) {
                this.tempalteName = tempalteName;
            }

            public String getTemplateAuthor() {
                return templateAuthor;
            }

            public void setTemplateAuthor(String templateAuthor) {
                this.templateAuthor = templateAuthor;
            }

            public String getTemplateVersion() {
                return templateVersion;
            }

            public void setTemplateVersion(String templateVersion) {
                this.templateVersion = templateVersion;
            }
        }

        private Definition.Metadata metadata = new Metadata();

        public Definition.Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Definition.Metadata metadata) {
            this.metadata = metadata;
        }
    }

    public static class Manifest{
        private boolean isNonManoAvailable;
        private List<SourcesParser.Source> sources = new ArrayList<>();
        private String cms;


        public static class Metadata {
            private String providerId;

            private String productName;

            private String releaseDateTime; //IETF RFC 3339

            private String packageVersion;

            public String getProviderId() {
                return providerId;
            }

            public void setProviderId(String providerId) {
                this.providerId = providerId;
            }

            public String getProductName() {
                return productName;
            }

            public void setProductName(String productName) {
                this.productName = productName;
            }

            public String getReleaseDateTime() {
                return releaseDateTime;
            }

            public void setReleaseDateTime(String releaseDateTime) {
                this.releaseDateTime = releaseDateTime;
            }

            public String getPackageVersion() {
                return packageVersion;
            }

            public void setPackageVersion(String packageVersion) {
                this.packageVersion = packageVersion;
            }
        }

        private Manifest.Metadata metadata = new Metadata();

        private Map<String, Map<String, List<String>>> nonMano = new HashMap<>();

        public Manifest.Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Manifest.Metadata metadata) {
            this.metadata = metadata;
        }

        public boolean isNonManoAvailable(){
            return isNonManoAvailable;
        }

        public Map<String, Map<String, List<String>>> getNonMano() {
            return nonMano;
        }

        public void setNonMano(Map<String, Map<String, List<String>>> nonMano) {
            this.nonMano = nonMano;
            this.isNonManoAvailable = true;
        }

        public List<SourcesParser.Source> getSources() {
            return Collections.unmodifiableList(sources);
        }

        public void setSources(List<SourcesParser.Source> sources) {
            this.sources.addAll(sources);
        }

        public String getCms() {
            return this.cms;
        }

        public void setCms(String cms) {
            this.cms = cms;
        }
    }

    private TOSCAMeta toscaMeta = new TOSCAMeta();

    private Definition definition = new Definition();

    private Manifest manifest;

    private File toscaMetaFile;

    private File definitionYamlFile;

    private File manifestMfFile;

    private File changeLogTxtFile;

    private File testsFolder;

    private File certificatesFile;

    private File licensesFolder;

    private List<CSARError> errors = new ArrayList<>();

    private FileArchive.Workspace workspace;

    protected Path tempDir;

    public CSARArchive(){
        this(new Manifest());
    }

    public CSARArchive(Manifest manifest) {
        this.manifest = manifest;
    }

    public FileArchive.Workspace getWorkspace() {
        return this.workspace;
    }

    public String getSOL004Version() {
        return SOL0004_2_4_1;
    }

    public TOSCAMeta getToscaMeta() {
        return toscaMeta;
    }

    public Definition getDefinition() {
        return definition;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public File getToscaMetaFile() {
        return toscaMetaFile;
    }

    public File getDefinitionYamlFile() {
        return definitionYamlFile;
    }

    public File getManifestMfFile() {
        return manifestMfFile;
    }

    public File getChangeLogTxtFile() {
        return changeLogTxtFile;
    }

    public File getTestsFolder() {
        return testsFolder;
    }

    public File getCertificatesFile() {
        return certificatesFile;
    }

    public File getLicensesFolder() {
        return licensesFolder;
    }

    public List<CSARError> getErrors() {
        return errors;
    }


    public String getProductName() {
        if (this.toscaMeta.getMode().equals(Mode.WITH_TOSCA_META_DIR)) {
            return this.manifest.getMetadata().getProductName();
        } else {
            return this.definition.getMetadata().getTemplateAuthor();
        }
    }

    public String getVendorName() {
        if (this.toscaMeta.getMode().equals(Mode.WITH_TOSCA_META_DIR)) {
            return this.toscaMeta.getCompanyName();
        } else {
            return this.definition.getMetadata().getTemplateAuthor();
        }
    }

    public String getVersion() {
        if (this.toscaMeta.getMode().equals(Mode.WITH_TOSCA_META_DIR)) {
            return this.manifest.getMetadata().getPackageVersion();
        } else {
            return this.definition.getMetadata().getTemplateVersion();
        }
    }

    private void setMode() {
        if (isToscaMetaFileExist()){
            this.toscaMeta.setMode(Mode.WITH_TOSCA_META_DIR);
        } else {
            this.toscaMeta.setMode(Mode.WITHOUT_TOSCA_META_DIR);
        }
    }

    private boolean isToscaMetaFileExist() {
        return new File(this.tempDir.toFile().getAbsolutePath() + File.separator +
                TOSCA_Metadata + File.separator + TOSCA_Metadata__TOSCA_Meta).exists();
    }

    void parseManifest() throws IOException {

        VnfManifestParser vnfManifestParser = VnfManifestParser.getInstance(
                this.getManifestMfFile()
        );

        Pair<Manifest.Metadata, List<CSARError>> metadataData = vnfManifestParser.fetchMetadata();
        Pair<List<SourcesParser.Source>, List<CSARError>> sourcesSectionData = vnfManifestParser.fetchSourcesSection();
        Pair<String, List<CSARError>> cmsSectionData = vnfManifestParser.fetchCMS();

        CSARArchive.Manifest manifest = this.getManifest();
        manifest.setMetadata(metadataData.getKey());
        this.getErrors().addAll(metadataData.getValue());

        manifest.setSources(sourcesSectionData.getKey());
        this.getErrors().addAll(sourcesSectionData.getValue());

        manifest.setCms(cmsSectionData.getKey());
        this.getErrors().addAll(cmsSectionData.getValue());

    }

    private void parseDefinitionMetadata() throws IOException {
        try(FileInputStream ipStream = new FileInputStream(this.definitionYamlFile)) {
            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);

            //yaml is empty or version string missing
            if (yaml == null || !yaml.keySet().contains(Entry_Definition__tosca_definitions_version)) {
                errors.add(
                        new CSARErrorEntryMissingToscaDefinitionVersion(
                                this.definitionYamlFile.getName()));
            } else {
                String version = (String) yaml.get(Entry_Definition__tosca_definitions_version);
                if (!Arrays.asList(Entry_Definition__tosca_definitions_versions).contains(version)) {
                    errors.add(new CSARErrorInvalidEntry(Entry_Definition__tosca_definitions_version,
                            this.definitionYamlFile.getName(), -1, "Should be " + Entry_Definition__tosca_definitions_version__simple_1_1));
                } else {
                    this.definition.setToscaDefinitionVersion(version);

                    if (this.toscaMeta.getMode().equals(Mode.WITHOUT_TOSCA_META_DIR)) {
                        //metadata section should be there
                        if (!yaml.keySet().contains(Entry_Definition__metadata)) {
                            errors.add(
                                    new CSARErrorInvalidEntryValueToscaDefinitionVersion(
                                            this.definitionYamlFile.getName()));
                        } else {
                            Map<String, String> metadata = (Map<String, String>) yaml.get(Entry_Definition__metadata);

                            for(Map.Entry<String, String> entry: metadata.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();

                                //continue till it reaches the metadata section
                                if (key.equalsIgnoreCase(Entry_Definition__template_author)) {
                                    this.definition.getMetadata().setTemplateAuthor(value);
                                } else if (key.equalsIgnoreCase(Entry_Definition__template_name)) {
                                    this.definition.getMetadata().setTempalteName(value);
                                } else if (key.equalsIgnoreCase(Entry_Definition__template_version)) {
                                    this.definition.getMetadata().setTemplateVersion(value);
                                } else {
                                    errors.add(
                                            new CSARErrorIgnored(
                                                    key,
                                                    this.definitionYamlFile.getName(),
                                                    -1,
                                                    null));
                                }
                            }

                            if (this.definition.getMetadata().getTemplateAuthor() == null) {
                                this.errors.add(
                                        new CSARErrorEntryMissingToscaDefinitionMetadataTemplateAuthor(
                                        this.definitionYamlFile.getName()));
                            }
                            if (this.definition.getMetadata().getTempalteName() == null) {
                                this.errors.add(new CSARErrorEntryMissingToscaDefinitionMetadataTemplateName(
                                        this.definitionYamlFile.getName()));
                            }

                            if (this.definition.getMetadata().getTemplateVersion() == null) {
                                this.errors.add(new CSARErrorEntryMissingToscaDefinitionMetadataTemplateVersion(
                                        this.definitionYamlFile.getName()));
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseMeta() throws IOException {
        if (this.toscaMeta.getMode().equals(Mode.WITH_TOSCA_META_DIR)) {
            this.toscaMetaFile = this.tempDir.resolve(TOSCA_Metadata+ File.separator + TOSCA_Metadata__TOSCA_Meta).toFile();

            int lineNo =0;
            for (String line: FileUtils.readLines(this.toscaMetaFile)) {
                lineNo ++;
                line = line.trim();

                if (line.startsWith("#")) {
                    continue;
                } else if (line.trim().isEmpty()) {
                    continue;
                }

                String []lineTokens = line.split(":");

                if (lineTokens.length != 2) {
                    errors.add(
                            new CSARErrorIgnored(
                                    line,
                                    TOSCA_Metadata__TOSCA_Meta,
                                    lineNo,
                                    null));
                    continue;
                }

                String key = lineTokens[0].trim();
                String value = lineTokens[1].trim();

                if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__TOSCA_Meta_File_Version)) {
                        this.toscaMeta.setMetaDataFileVersion(value);
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__CSAR_Version)){
                        this.toscaMeta.setCsarVersion(value);
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__Created_by)) {
                        this.toscaMeta.setCompanyName(value);
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__Entry_Definitions)) {
                        this.toscaMeta.setEntryDefinitionYaml(value);
                        this.definitionYamlFile = new File(this.tempDir.toFile().getAbsolutePath() + File.separator + (this.toscaMeta.getEntryDefinitionYaml()));

                        if (!this.definitionYamlFile.exists()) {
                            errors.add(
                                    new CSARErrorInvalidEntryValueToscaDefinitionNotFound(
                                            this.toscaMeta.getEntryDefinitionYaml(),
                                            lineNo));
                        }
                } else if(key.equalsIgnoreCase(getEntryManifestParamName())) {
                        this.toscaMeta.setEntryManifestMf(value);
                        this.manifestMfFile = this.tempDir.resolve(this.toscaMeta.getEntryManifestMf()).toFile();
                        if (!this.manifestMfFile.exists()) {
                            errors.add(new CSARErrorInvalidEntryValueManifestNotFound(
                                    this.toscaMeta.getEntryManifestMf(),
                                    lineNo, getEntryManifestParamName()));
                        }
                } else if(key.equalsIgnoreCase(getEntryChangeLogParamName())) {
                        this.toscaMeta.setEntryChangeLog(value);
                        this.changeLogTxtFile = this.tempDir.resolve(this.toscaMeta.getEntryChangeLog()).toFile();
                        if (!this.changeLogTxtFile.exists()) {
                            errors.add(new CSARErrorInvalidEntryValueLogsNotFound(
                                    this.toscaMeta.getEntryChangeLog(),
                                    lineNo, getEntryChangeLogParamName()));
                        }
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__Entry_Tests)) {
                        this.toscaMeta.setEntryTest(value);
                        this.testsFolder= this.tempDir.resolve(this.toscaMeta.getEntryTest()).toFile();
                        if (!this.testsFolder.exists() || !this.testsFolder.isDirectory()) {
                            errors.add(new CSARErrorInvalidEntryValueTestsNotFound(
                                    this.toscaMeta.getEntryTest(),
                                    lineNo));
                        }
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__Entry_Licenses)) {
                        this.toscaMeta.setEntryLicense(value);
                        this.licensesFolder= this.tempDir.resolve(this.toscaMeta.getEntryLicense()).toFile();
                        if (!this.licensesFolder.exists() || !this.licensesFolder.isDirectory()) {
                            errors.add(new CSARErrorInvalidEntryValueLicenseNotFound(
                                    this.toscaMeta.getEntryLicense(),
                                    lineNo));
                        }
                } else if(key.equalsIgnoreCase(TOSCA_Metadata__TOSCA_Meta__Entry_Certificate)) {
                        this.toscaMeta.setEntryCertificate(value);
                        this.certificatesFile= this.tempDir.resolve(this.toscaMeta.getEntryCertificate()).toFile();
                        if (!this.certificatesFile.exists()) {
                            errors.add(new CSARErrorInvalidEntryValueCertificatesNotFound(
                                    this.toscaMeta.getEntryCertificate(),
                                    lineNo));
                        }
                } else {
                        errors.add(
                                new CSARErrorIgnored(
                                        key,
                                        TOSCA_Metadata__TOSCA_Meta,
                                        lineNo,
                                        null));
                }
            }

            if (this.toscaMeta.getMetaDataFileVersion() == null) {
                errors.add(new CSARErrorEntryMissingToscaMetaFileVersion());

                //should the file version value to be checked ??
            }

            if (this.toscaMeta.getEntryDefinitionYaml() == null) {
                errors.add(new CSARErrorEntryMissingToscaMetaDefinition());
            }

            if (this.toscaMeta.getCsarVersion() == null) {
                errors.add(new CSARErrorEntryMissingToscaMetaCSARVersion());
            }

            if (this.toscaMeta.getCompanyName() == null) {
                errors.add(new CSARErrorEntryMissingToscaMetaCreatedBy());
            }

        } else {
            //definition files
            File []files = this.tempDir.toFile().listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".yaml");
                }
            });

            if (files.length == 0) {
                errors.add(
                        new CSARErrorEntryMissingToscaDefinitionNotFound());
            }else if (files.length > 1) {
                List<String> fileNames = new ArrayList<>();
                for (File f: files) {
                    fileNames.add(f.getName());
                }
                errors.add(
                        new CSARErrorConflictsMultipleDefinitionYamls(fileNames.toString()));
            } else {
                this.definitionYamlFile = files[0];
                this.toscaMeta.setEntryDefinitionYaml(this.definitionYamlFile.getName());

                //manifest
                files = this.tempDir.toFile().listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".mf");
                    }
                });

                if (files.length > 1) {
                    List<String> fileNames = new ArrayList<>();
                    for (File f: files) {
                        fileNames.add(f.getName());
                    }
                    errors.add(new CSARErrorConflictsMultipleManifests(fileNames.toString()));
                } else {
                    this.manifestMfFile = files[0];
                    this.toscaMeta.setEntryManifestMf(this.manifestMfFile.getName());

                    //name should match the definition yaml
                    String defYaml = this.toscaMeta.getEntryDefinitionYaml().substring(
                            0, this.toscaMeta.getEntryDefinitionYaml().lastIndexOf(".yaml"));
                    String mfFile = this.toscaMeta.getEntryManifestMf().substring(
                            0, this.toscaMeta.getEntryManifestMf().lastIndexOf(".mf"));

                    if (!defYaml.equalsIgnoreCase(mfFile)) {
                        errors.add(new CSARErrorMismatchDefinitionYamlVsManifestMf(
                                defYaml,
                                mfFile));
                    }

                }

                //certificate
                files = this.tempDir.toFile().listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".cert");
                    }
                });

                if (files.length > 1) {
                    List<String> fileNames = new ArrayList<>();
                    for (File f: files) {
                        fileNames.add(f.getName());
                    }
                    errors.add(
                            new CSARErrorConflictsMultipleCertificates(fileNames.toString()));
                } else {
                    this.certificatesFile = files[0];
                    this.toscaMeta.setEntryCertificate(this.certificatesFile.getName());

                    //name should match the definition yaml
                    String defYaml = this.toscaMeta.getEntryDefinitionYaml().substring(
                            0, this.toscaMeta.getEntryDefinitionYaml().lastIndexOf(".yaml"));
                    String certFile = this.toscaMeta.getEntryCertificate().substring(
                            0, this.toscaMeta.getEntryCertificate().lastIndexOf(".cert"));

                    if (!defYaml.equalsIgnoreCase(certFile)) {
                        errors.add(new CSARErrorMismatchDefinitionYamlVsCertificateCert(
                                defYaml,
                                certFile ));
                    }
                }
            }



            for (File file: this.tempDir.toFile().listFiles()) {
                if (file.getName().equalsIgnoreCase(Change_Logs_Txt)) {
                    this.changeLogTxtFile = file;
                }

                else if (file.getName().equalsIgnoreCase(Tests)) {
                    this.testsFolder = file;
                }

                else if (file.getName().equalsIgnoreCase(Licenses)) {
                    this.licensesFolder = file;
                }

                else {
                    errors.add(
                            new CSARErrorIgnored(
                                    file.getName(),
                                    CSAR_Archive,
                                    -1,
                                    null));
                }
            }
        }
    }

    String getEntryManifestParamName(){
        return TOSCA_Metadata__TOSCA_Meta__Entry_Manifest;
    }

    String getEntryChangeLogParamName(){
        return TOSCA_Metadata__TOSCA_Meta__Entry_Change_Log;
    }

    public void init(String csarPath) throws IOException {
        this.workspace = new FileArchive(TEMP_DIR).unpack(csarPath);

        final Optional<Path> pathToCsarFolder = workspace.getPathToCsarFolder();
        if (pathToCsarFolder.isPresent()) {
            this.tempDir = pathToCsarFolder.get();
        }
    }

    public void parse() throws IOException {
        //Find out the mode of CSAR
        this.setMode();

        //process the TOSCA.meta
        this.parseMeta();

        //process manifest
        if(isFileExists(this.manifestMfFile)) {
            this.parseManifest();
        }

        //process definition
        if(isFileExists(this.definitionYamlFile)){
            this.parseDefinitionMetadata();
        }
    }

    private boolean isFileExists(File file) {
        return !Objects.isNull(file) && file.exists();
    }

    public void cleanup() throws IOException {
        //remove temp dir
        final Optional<Path> rootFolder = workspace.getRootFolder();
        if(rootFolder.isPresent()) {
            FileUtils.deleteDirectory(rootFolder.get().toFile());
        }
    }

    public File getFileFromCsar(String path) {
        return new File(this.tempDir.toFile().getAbsolutePath() + File.separator + path);
    }

    @Override
    public void close() throws Exception {
        cleanup();
    }
}
