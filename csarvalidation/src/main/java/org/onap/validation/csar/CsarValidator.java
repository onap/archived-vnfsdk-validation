/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.onap.validation.csar;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class CsarValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CsarValidator.class);

    // Schema files
    static private ValidatorSchemaLoader vsl;

    // Map of CSAR file and un-zipped file indices
    static private HashMap<String, String> csarFiles;

    // Map of packageId and CSAR files
    private static HashMap<String, HashMap<String, String>> csar = new HashMap<String, HashMap<String, String>>();

    private static String MAINSERV_TEMPLATE = CommonConstants.MAINSERV_TEMPLATE;

    private static String MAINSERV_MANIFEST;

    /**
     * @param packageId
     * @param csarWithPath
     * @throws IOException
     */
    public CsarValidator(String packageId, String csarWithPath) throws IOException {

        try (FileInputStream is = new FileInputStream(csarWithPath)) {

        } catch(FileNotFoundException e2) {
            LOG.error(csarWithPath + ":CSAR  is not found! " + ErrorCodes.RESOURCE_MISSING, e2);
            throw new ValidationException(ErrorCodes.RESOURCE_MISSING,
                    "RESOURCE MISSING" + csarWithPath + ":CSAR  is not found!");
        }

        try {
            csarFiles = CsarUtil.csarExtract(csarWithPath);
            if(!csarFiles.isEmpty()) {
                csar.put(packageId, getCsarFiles());
                LOG.debug("CSAR extracted sucessfully.");
            }
        } catch(Exception e1) {
            LOG.error("INVALID_CSAR_CONTENT" + ":" + csarWithPath + ": CSAR is not a valid CSAR/ZIP file! "
                    + ErrorCodes.INVALID_CSAR_CONTENT, e1);
            throw new ValidationException(ErrorCodes.INVALID_CSAR_CONTENT,
                    "INVALID_CSAR_CONTENT" + ":" + csarWithPath + ": CSAR is not a valid CSAR/ZIP file! ");
        }

        try {
            vsl = new ValidatorSchemaLoader();
        } catch(Exception e) {
            LOG.error(
                    "SCHEMA_LOAD_ERROR" + ":" + "CSAR schema is not loaded correctly! " + ErrorCodes.SCHEMA_LOAD_ERROR,
                    e);
            throw new ValidationException(ErrorCodes.SCHEMA_LOAD_ERROR,
                    "SCHEMA_LOAD_ERROR" + ":" + "CSAR schema is not loaded correctly! ");
        }
    }

    /**
     * @return true if all validations are successful
     */
    public static String validateCsar() {

        String vsm = validateCsarMeta();

        String vtm = validateAndScanToscaMeta();

        String vms = validateMainService();

        if((CommonConstants.SUCCESS_STR != vsm) && (CommonConstants.SUCCESS_STR != vms)) {

            return vsm + " OR " + vms;
        }

        if(CommonConstants.SUCCESS_STR != vtm) {
            return vtm;
        }

        return CommonConstants.SUCCESS_STR;
    }

    /**
     * @param csarWithPath
     * @return true if csar integrity validation is successful
     */
    public static boolean validateCsarIntegrity(String csarWithPath) {

        try {
            RandomAccessFile raf = new RandomAccessFile(csarWithPath, "r");
            try {
                long n = raf.readInt();

                // Check for the CSAR's integrity
                if(n != 0x504B0304) {
                    LOG.error("CSAR %s contents are not a valid! ");
                    return false;
                }
            } catch(FileNotFoundException e1) {
                LOG.error("CSAR %s is not a valid CSAR/ZIP file! ", e1);
                return false;
            } finally {
                raf.close();
            }
        } catch(IOException e1) {
            LOG.error("CSAR %s is not a valid CSAR/ZIP file! ", e1);
            return false;
        }
        return true;
    }

    /**
     * @return true if csar meta data validation is successful
     */
    public static String validateCsarMeta() {

        String cfile = csarFiles.get(CommonConstants.CSAR_META);
        if(null == cfile) {
            return "VNF package do not contain csar.meta file";
        }
        if(!cfile.isEmpty()) {
            File file = new File(cfile);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String tempString = null;
                while((tempString = reader.readLine()) != null) {
                    if(!tempString.equals("")) {
                        int count1 = tempString.indexOf(":");
                        String meta = tempString.substring(0, count1).trim();
                        if(meta.equalsIgnoreCase(CommonConstants.CSAR_TYPE_META)) {
                            int count = tempString.indexOf(":") + 1;
                            if(tempString.substring(count).trim().isEmpty()) {
                                return "Validating csar.meta:Type field is in wrong format or empty";
                            }
                        }
                        if(meta.equalsIgnoreCase(CommonConstants.CSAR_PROVIDER_META)) {
                            int count = tempString.indexOf(":") + 1;
                            if(tempString.substring(count).trim().isEmpty()) {
                                return "Validating csar.meta:Provider field is in wrong format or empty";
                            }
                        }
                        if(meta.equalsIgnoreCase(CommonConstants.CSAR_VERSION_META)) {
                            int count = tempString.indexOf(":") + 1;
                            if(tempString.substring(count).trim().isEmpty()) {
                                return "Validating csar.meta:Version field is in wrong format or empty";
                            }
                        }
                    }
                    reader.close();
                    return CommonConstants.SUCCESS_STR;
                }
            } catch(IOException e2) {
                return ("CSAR_META_VALIDATION" + ":" + "Exception caught while validateCsarMeta ! " + ErrorCodes.FILE_IO
                        + e2);
            }
        }

        return "VNF package do not contain csar.meta file";
    }

    /**
     * @return true csar tosca meta validation is successful
     */
    public static String validateAndScanToscaMeta() {

        String cfile = csarFiles.get(CommonConstants.TOSCA_META);

        if(null == cfile) {
            return "Tosca.meta file does not exist";
        }

        if(!validateToscaMeta(cfile)) {
            return "Validation of Tosca.meta file fails";
        }

        try {
            if(cfile.contains(System.getProperty("file.separator") + CommonConstants.TOSCA_METADATA
                    + System.getProperty("file.separator") + CommonConstants.TOSCA_META)) {
                MAINSERV_MANIFEST = checkAndGetMRF(cfile, "Entry-Manifest");
                if(MAINSERV_MANIFEST == null) {
                    MAINSERV_MANIFEST = CommonConstants.MAINSERV_MANIFEST;
                }

                MAINSERV_TEMPLATE = checkAndGetMRF(cfile, "Entry-Definitions");
                if(MAINSERV_TEMPLATE == null) {
                    MAINSERV_TEMPLATE = CommonConstants.MAINSERV_TEMPLATE;
                }

                return CommonConstants.SUCCESS_STR;
            }
        } catch(Exception e) {
            return ("PARSE_ERROR" + ":" + "TOSCA metadata not parsed properly! " + ErrorCodes.PARSE_ERROR + e);
        }

        return "Tosca.meta file does not exist";
    }

    private static String checkAndGetMRF(String mrfFile, String attribute) {
        try {
            String value = checkEntryFor(mrfFile, attribute);
            String mrfCsarEntry = null;

            // Rel-1 & SOL004 Entry-Definitions is optional
            if(!StringUtils.isEmpty(value)) {

                if(value.contains("Definitions/")) {
                    String[] splitPath = value.split("/");
                    mrfCsarEntry = csarFiles.get(splitPath[splitPath.length - 1]);
                } else { // Hack to support non-compliant "Entry-Definitions:" format
                    mrfCsarEntry = csarFiles.get(value);
                }

                if(null != mrfCsarEntry) {
                    return mrfCsarEntry;
                }
            }
        } catch(IOException | NullPointerException e) {
            LOG.error("CSAR_TOSCA_VALIDATION" + ":" + "Could not read file %s ! " + ErrorCodes.FILE_IO + " "
                    + ErrorCodes.RESOURCE_MISSING, e);
            throw new ValidationException(ErrorCodes.RESOURCE_MISSING);
        }

        return null;
    }

    /**
     * @return true csar validation is successful
     */
    public static String validateMainService() {

        String key = "metadata";

        // In future load from the respective file template/schema
        List<String> mListMetadata =
                Arrays.asList("vnf_product_name", "vnf_provider_id", "vnf_package_version", "vnf_release_data_time");

        @SuppressWarnings("unused")
        boolean mfResult = checkEntryFor(CommonConstants.MAINSERV_MANIFEST, mListMetadata, key);

        String mainServManifest = MAINSERV_MANIFEST;
        if(!Paths.get(mainServManifest).isAbsolute()) {
            mainServManifest = csarFiles.get(FilenameUtils.getName(mainServManifest));
        }
        // Rel-2 SOL004 requirement
        if(StringUtils.isEmpty(mainServManifest)) {
            // Do nothing for Rel-1
            // return false;
        }

        String mainservTemplate = MAINSERV_TEMPLATE;
        if(!Paths.get(MAINSERV_TEMPLATE).isAbsolute()) {
            mainservTemplate = csarFiles.get(FilenameUtils.getName(mainservTemplate));
        }
        if(StringUtils.isEmpty(mainservTemplate)) {
            return "MainServiceTemplate does not exist in the package";
        }
        return CommonConstants.SUCCESS_STR;
    }

    private static String checkEntryFor(String fileWithPath, String attribute) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(fileWithPath), UTF_8);

        for(String strLine : lines) {
            if(!attribute.isEmpty() && strLine.contains(attribute)) {
                String entry = strLine.substring(attribute.length(), strLine.length()).trim();
                return entry.replaceFirst(":", "").trim();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static boolean checkEntryFor(String cFile, List<String> attributes, String key) {
        @SuppressWarnings("unused")
        String tFileWithPath;

        if(!Paths.get(cFile).isAbsolute()) {
            cFile = csarFiles.get(FilenameUtils.getName(cFile));
        }

        if(StringUtils.isEmpty(cFile)) {
            return false;
        }

        Yaml yaml = new Yaml();
        Map<String, ?> values;
        try (InputStream input = new FileInputStream(new File(cFile))) {
            values = (Map<String, ?>)yaml.load(input);
        } catch(FileNotFoundException e) {
            LOG.error("FILE_NOT_FOUND" + ":" + "Exception caught while trying to find the file ! " + e.getMessage(), e);
            return false;
        } catch(IOException e1) {
            LOG.error("FILE_NOT_FOUND" + ":" + "Exception caught while trying to open the file ! " + e1.getMessage(),
                    e1);
            return false;
        }

        Map<? super String, ? super String> subValues = (Map<? super String, ? super String>)values.get(key);

        // 1. Check for empty values in map and if number of mandatory attributes presence
        List<? super String> lResultNonNull =
                subValues.values().stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        // If no attributes provided, take it easy
        // there are no mandatory fields to be
        // validated.
        if(attributes == null) {
            return true;
        }

        if(subValues.size() != attributes.size() && lResultNonNull.size() != attributes.size()) {
            return false;
        }

        // Validate the exact mandatory(not defined in SOL004 yet)
        // attributes with expected attributes list
        List<? super String> lResult =
                subValues.keySet().stream().filter(attributes::contains).collect(Collectors.toList());

        if(lResult.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * @return true if csar meta data validation is successful
     */
    @SuppressWarnings({"unchecked", "static-access"})
    private static boolean validateToscaMeta(String cfile) {

        if(StringUtils.isEmpty(cfile)) {
            return false;
        } else {
            File file = new File(cfile);

            Yaml yaml = new Yaml();

            Map<String, ?> toscaMeta = null;
            try (InputStream input = new FileInputStream(new File(cfile))) {
                toscaMeta = (Map<String, ?>)yaml.load(input);
            } catch(FileNotFoundException e) {
                LOG.error("CSAR_TOSCA_LOAD" + ":" + "TOSCA metadata is not loaded by Yaml! " + ErrorCodes.FILE_IO, e);
            } catch(IOException e1) {
                LOG.error("CSAR_TOSCA_LOAD" + ":" + "TOSCA metadata is not loaded by Yaml! " + ErrorCodes.FILE_IO, e1);
            }
            if(toscaMeta != null) {
                return toscaMeta.keySet().containsAll((vsl.getToscaMeta().keySet()));
            }
            return false;
        }
    }

    public static HashMap<String, HashMap<String, String>> getCsar() {
        return csar;
    }

    public static void setCsar(HashMap<String, HashMap<String, String>> csar) {
        CsarValidator.csar = csar;
    }

    public static HashMap<String, String> getCsarFiles() {
        return csarFiles;
    }

    public static void setCsarFiles(HashMap<String, String> csarFiles) {
        CsarValidator.csarFiles = csarFiles;
    }
}
