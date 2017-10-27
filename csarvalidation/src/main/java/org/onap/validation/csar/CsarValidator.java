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


import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.charset.StandardCharsets.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class CsarValidator {

	private static final Logger LOG = LoggerFactory.getLogger(CsarValidator.class);

	//Schema files
    static private ValidatorSchemaLoader vsl;

	// Map of CSAR file and un-zipped file indices
	static private HashMap<String, String> csarFiles;

    //  Map of packageId and CSAR files
	private static HashMap<String, HashMap<String, String>> csar = new HashMap<String, HashMap<String, String>>();
    private static String MAINSERV_TEMPLATE = CommonConstants.MAINSERV_TEMPLATE;
    private static String MAINSERV_MANIFEST;

    /**
     * 
     * @param packageId
     * @param csarWithPath
     * @throws IOException 
     */
    public CsarValidator(String packageId, String csarWithPath) throws IOException {

		try(FileInputStream is = new FileInputStream(csarWithPath)) {
			
		} catch (FileNotFoundException e2) {
			LOG.error("CSAR %s is not found! " +ErrorCodes.RESOURCE_MISSING, e2);
            throw new ValidationException(ErrorCodes.RESOURCE_MISSING);
		}

		try {
			csarFiles = CsarUtil.csarExtract(csarWithPath);
			if(!csarFiles.isEmpty()) {
                csar.put(packageId, getCsarFiles());
				LOG.debug("CSAR extracted sucessfully.");
			}
		} catch (Exception e1) {
			LOG.error("INVALID_CSAR_CONTENT" + ":" + "CSAR %s is not a valid CSAR/ZIP file! " +ErrorCodes.INVALID_CSAR_CONTENT, e1);
            throw new ValidationException(ErrorCodes.INVALID_CSAR_CONTENT);
		}


        try {
               vsl = new ValidatorSchemaLoader();
        } catch (Exception e) {
          	 LOG.error("SCHEMA_LOAD_ERROR" + ":" + "CSAR schema is not loaded correctly! " +ErrorCodes.SCHEMA_LOAD_ERROR, e);
             throw new ValidationException(ErrorCodes.SCHEMA_LOAD_ERROR);
        }
    }
    /**
     * 
     * @return true if all validations are successful
     */
    public static boolean validateCsar() {

        boolean vsm = validateCsarMeta();

        boolean vtm = validateAndScanToscaMeta();

        boolean vms = validateMainService();

        if ((vsm || vms ) && vtm) {
            return true;
        }


        return false;
    }
    
    /**
     * 
     * @param csarWithPath
     * @return true if csar integrity validation is successful
     */
    public static boolean validateCsarIntegrity(String csarWithPath) {

		try {
			RandomAccessFile raf = new RandomAccessFile(csarWithPath, "r");
			long n = raf.readInt();
			raf.close();

			// Check for the CSAR's integrity
			if (n != 0x504B0304) {
				LOG.error("CSAR %s is not a valid CSAR/ZIP file! ");
				return false;
			}
			return true;
		} catch (IOException e1) {
			LOG.error("CSAR %s is not a valid CSAR/ZIP file! ", e1);
			return false;
		}
	}

    /**
     * 
     * @return true if csar meta data validation is successful
     */
	public static boolean validateCsarMeta() {

		String cfile = csarFiles.get(CommonConstants.CSAR_META);
        if (StringUtils.isEmpty(cfile)) {
            return false;
        }
		if (!cfile.isEmpty()) {
				File file = new File(cfile);

				try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String tempString = null;
					while ((tempString = reader.readLine()) != null) {
						if (!tempString.equals("")) {
							int count1 = tempString.indexOf(":");
							String meta = tempString.substring(0, count1).trim();
							if (meta.equalsIgnoreCase(CommonConstants.CSAR_TYPE_META)) {
								int count = tempString.indexOf(":") + 1;
								if (tempString.substring(count).trim().isEmpty()) {
									return false;
								}
							}
							if (meta.equalsIgnoreCase(CommonConstants.CSAR_PROVIDER_META)) {
								int count = tempString.indexOf(":") + 1;
								if (tempString.substring(count).trim().isEmpty()) {
									return false;
								}
							}
							if (meta.equalsIgnoreCase(CommonConstants.CSAR_VERSION_META)) {
								int count = tempString.indexOf(":") + 1;
								if (tempString.substring(count).trim().isEmpty()) {
									return false;
								}
							}
						}
				    reader.close();
			    	return true;
				  }
				} catch (IOException e2) {
                    LOG.error("CSAR_META_VALIDATION" + ":" + "Exception caught while validateCsarMeta ! " +ErrorCodes.FILE_IO, e2);
		            throw new ValidationException(ErrorCodes.FILE_IO);
				} 
		}

        return false;
    }

	/**
	 * 
	 * @return true csar tosca meta validation is successful
	 */
    public static boolean validateAndScanToscaMeta() {

        String cfile = csarFiles.get(CommonConstants.TOSCA_META);

        if (!validateToscaMeta(cfile)) {
            return false;
        }

        try {
            if (cfile.contains(System.getProperty("file.separator") +
                    CommonConstants.TOSCA_METADATA + System.getProperty("file.separator") +
                    CommonConstants.TOSCA_META)) {
                MAINSERV_MANIFEST = checkAndGetMRF(cfile,"Entry-Manifest");
                if (MAINSERV_MANIFEST == null) {
                    MAINSERV_MANIFEST = CommonConstants.MAINSERV_MANIFEST;
                }

                MAINSERV_TEMPLATE = checkAndGetMRF(cfile,"Entry-Definitions");
                if (MAINSERV_TEMPLATE == null) {
                    MAINSERV_TEMPLATE = CommonConstants.MAINSERV_TEMPLATE;
                }

                return true;
            }
        } catch (Exception e) {
       	 LOG.error("PARSE_ERROR" + ":" + "TOSCA metadata not parsed properly! " +ErrorCodes.PARSE_ERROR, e);
        }

        return false;
    }

    private static String checkAndGetMRF(String mrfFile, String attribute) {
        try {
        String value = CheckEntryFor(mrfFile, attribute);
        String mrfCsarEntry = null;

        //Rel-1 & SOL004 Entry-Definitions is optional
        if (! StringUtils.isEmpty(value)) {

            if(value.contains("Definitions/"))
            {
                String[] splitPath = value.split("/");
                mrfCsarEntry = csarFiles.get(splitPath[splitPath.length - 1]);
            }
            else {  //Hack to support non-compliant "Entry-Definitions:" format
                mrfCsarEntry = csarFiles.get(value);
            }

            if (null != mrfCsarEntry) {
                return mrfCsarEntry;
            }
        }
    } catch (IOException | NullPointerException e) {
        LOG.error("CSAR_TOSCA_VALIDATION" + ":" + "Could not read file %s ! " +ErrorCodes.FILE_IO+ " " +ErrorCodes.RESOURCE_MISSING);
        throw new ValidationException(ErrorCodes.RESOURCE_MISSING);
        }

        return null;
    }

    /**
     * 
     * @return true csar validation is successful
     */
    public static boolean validateMainService() {

        String key = "metadata";

        // In future load from the respective file template/schema
        List<String> mListMetadata = Arrays.asList("vnf_product_name", "vnf_provider_id",
                "vnf_package_version", "vnf_release_data_time");

        @SuppressWarnings("unused")
		boolean mfResult = CheckEntryFor(CommonConstants.MAINSERV_MANIFEST, mListMetadata, key);

        String mainServManifest = MAINSERV_MANIFEST;
        if(!Paths.get(mainServManifest).isAbsolute()){
            mainServManifest = csarFiles.get(FilenameUtils.getName(mainServManifest));
        }
        // Rel-2 SOL004 requirement
        if(StringUtils.isEmpty(mainServManifest)){
            //Do nothing for Rel-1
            //return false;
        }

        String mainservTemplate = MAINSERV_TEMPLATE;
        if(!Paths.get(MAINSERV_TEMPLATE).isAbsolute()){
        	mainservTemplate = csarFiles.get(FilenameUtils.getName(mainservTemplate));
        }
        if(StringUtils.isEmpty(mainservTemplate)){
        	return false;
        }
        return true;
    }

    private static String CheckEntryFor(String fileWithPath, String attribute) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(fileWithPath), UTF_8);

        for (String strLine : lines) {
            if (!attribute.isEmpty() && strLine.contains(attribute)) {
                String entry = strLine.substring(attribute.length(), strLine.length()).trim();
                return entry.replaceFirst(":","").trim();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	private static boolean CheckEntryFor(String cFile, List<String> attributes, String key) {
        @SuppressWarnings("unused")
		String tFileWithPath;

        if (! Paths.get(cFile).isAbsolute()) {
            cFile = csarFiles.get(FilenameUtils.getName(cFile));
        }

        if(StringUtils.isEmpty(cFile)) {
            return false;
        }

        Yaml yaml = new Yaml();
        Map<String, ?> values;
        try {
            values = (Map<String, ?>) yaml.load(new FileInputStream(new File(cFile)));
        } catch (FileNotFoundException e) {
        	LOG.error("FILE_NOT_FOUND" + ":" + "Exception caught while trying to find the file ! " + e.getMessage(), e);
            return false;
        }

        Map<? super String,? super String> subValues = (Map<? super String, ? super String>) values.get(key);

        //1. Check for empty values in map and if number of mandatory attributes presence
        List<? super String> lResultNonNull = subValues.values().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // If no attributes provided, take it easy
        // there are no mandatory fields to be
        // validated.
        if (attributes == null) {
            return true;
        }

        if (subValues.size() != attributes.size() &&
		    lResultNonNull.size() != attributes.size()) {
            return false;
        }

        //Validate the exact mandatory(not defined in SOL004 yet)
        // attributes with expected attributes list
        List<? super String> lResult = subValues.keySet().stream()
                .filter(attributes::contains)
                .collect(Collectors.toList());

        if (lResult.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return true if csar meta data validation is successful
     */
    @SuppressWarnings({ "unchecked", "static-access" })
	private static boolean validateToscaMeta(String cfile) {

        if (StringUtils.isEmpty(cfile)) {
            return false;
        }
        else {
            File file = new File(cfile);

            Yaml yaml = new Yaml();


            Map<String, ?> toscaMeta = null;
            try {
                toscaMeta = (Map<String, ?>) yaml.load(new FileInputStream(file));
            } catch (FileNotFoundException e) {
            	 LOG.error("CSAR_TOSCA_LOAD" + ":" + "TOSCA metadata is not loaded by Yaml! " +ErrorCodes.FILE_IO, e);
            }
            return toscaMeta.keySet().containsAll((vsl.getToscaMeta().keySet()));
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

