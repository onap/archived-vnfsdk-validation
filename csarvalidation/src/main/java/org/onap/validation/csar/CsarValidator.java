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

	// Map of CSAR file and un-zipped file indices
	static HashMap<String, String> csarFiles;

    //  Map of packageId and CSAR files
	private static HashMap<String, HashMap<String, String>> csar = new HashMap<String, HashMap<String, String>>(); 
	
	private static final CsarUtil cUtil = new CsarUtil();

	public CsarValidator(String packageId, String csarWithPath) {

		try {
			FileInputStream is = new FileInputStream(csarWithPath);
		} catch (FileNotFoundException e2) {
			LOG.error("CSAR %s is not found! ", e2);
		}
		try {
			csarFiles = CsarUtil.csarExtract(csarWithPath);
			if(!csarFiles.isEmpty()) {
                csar.put(packageId, getCsarFiles());
				LOG.debug("CSAR extracted sucessfully.");
			}
		} catch (Exception e1) {
			LOG.error("CSAR %s is not a valid CSAR/ZIP file! ", e1);
		}

	}

    public static boolean validateCsar() {

        boolean vsm = validateCsarMeta();

        boolean vtm = validateToscaMeta();

        boolean vms = validateMainService();

        if ((vsm || vms ) && vtm) {
            return true;
        }

        //In future return the status handler object instead.
        return false;
    }

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
					LOG.error("Exception caught while validateCsarMeta ! " + e2.getMessage());
				} 
		}


        return false;
    }


    public static boolean validateToscaMeta() {

        String cfile = csarFiles.get(CommonConstants.TOSCA_META);
        if(StringUtils.isEmpty(cfile)) {
            return false;
        }
        try {
            if (!cfile.isEmpty() && cfile.contains(System.getProperty("file.separator") +
                    CommonConstants.TOSCA_METADATA + System.getProperty("file.separator") +
                    CommonConstants.TOSCA_META)) {
                String value = checkEntryFor(cfile, "Entry-Definitions:");
                String[] splitPath = value.split("/"); 
                String subValue = splitPath[splitPath.length - 1];

                if (value.isEmpty() || subValue.isEmpty()) {
                    return false;
                    //Check if Entry-Defintions pointed file exists in CSAR

                } else if (!(null == csarFiles.get(value)) || 
				           !(null == csarFiles.get(subValue))) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOG.error("Could not read file %s ! " + e.getMessage(), cfile);
        }
        return false;
    }

    public static boolean validateMainService() {

        String key = "metadata";

        // Infuture load from the respective file template/schema
        List<String> mListMetadata = Arrays.asList("vnf_product_name", "vnf_provider_id",
                "vnf_package_version", "vnf_release_data_time");
        boolean mfResult = checkEntryFor(CommonConstants.MAINSERV_MANIFEST, mListMetadata, key);

        List<String> tListMetadata = Arrays.asList("vendor", "csarVersion",
                "csarProvider", "id", "version", "csarType", "name", "vnfdVersion",
                "vnfmType");
        boolean tResult = checkEntryFor(CommonConstants.MAINSERV_TEMPLATE, tListMetadata, key);

        if (tResult && mfResult) {
            return true;
        } else {
            return false;
        }
    }

    private static String checkEntryFor(String fileWithPath, String attribute) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(fileWithPath), UTF_8);

        for (String strLine : lines) {
            if (!attribute.isEmpty() && strLine.contains(attribute)) {
                return strLine.substring(attribute.length(), strLine.length()).trim();
            }
        }
        return null;
    }

    private static boolean checkEntryFor(String cFile, List<String> attributes, String key) {
        String tFileWithPath = csarFiles.get(cFile);

        if(StringUtils.isEmpty(tFileWithPath)) {
            return false;
        }

        Yaml yaml = new Yaml();
        Map<String, ?> values = null;
        try {
            values = (Map<String, ?>) yaml.load(new FileInputStream(new File(tFileWithPath)));
        } catch (FileNotFoundException e) {
            return false;
        }

        Map<String,String> subValues = (Map<String,String>) values.get(key);

        //1. Check for empty values in map and if number of mandatory attributes presence
        List<String> lResultNonNull = subValues.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (subValues.size() != attributes.size() && 
		    lResultNonNull.size() != attributes.size()) {
            return false;
        }

        //2. Validate the exact mandatory attributes with expected attributes list
        List<? super String> lResult = subValues.keySet().stream()
                .filter(attributes::contains)
                .collect(Collectors.toList());

        // System.out.println(result);
        if (lResult.size() != attributes.size()) {
            return false;
        }
        return true;
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

