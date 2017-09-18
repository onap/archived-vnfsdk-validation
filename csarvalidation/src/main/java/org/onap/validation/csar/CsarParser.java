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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import java.util.*;

import static java.nio.charset.StandardCharsets.*;


public class CsarParser {

	private static final Logger LOG = LoggerFactory.getLogger(CsarParser.class);

	// Map of CSAR file and un-zipped file indices
	private static HashMap<String, String> csarFiles;
	
	//  Map of packageId and CSAR files
	private static HashMap<String, HashMap<String, String>> csar = new HashMap<String, HashMap<String, String>>(); 
	
	private static final CsarUtil cUtil = new CsarUtil();

	public CsarParser(String csarWithPath) {

		try {
			FileInputStream is = new FileInputStream(csarWithPath);
		} catch (FileNotFoundException e2) {
			LOG.error("CSAR %s is not found! ", e2);
		}
		try {
			boolean ret = csarExtract(csarWithPath);
			if(ret == true) {
				LOG.debug("CSAR extracted sucessfully.");
			}
		} catch (Exception e1) {
			LOG.error("CSAR %s is not a valid CSAR/ZIP file! ", e1);
		}
	}
	/*
	 * pubic static boolean validateCsar(String filePath) {
	 * 
	 * csarExtract(filePath);
	 * 
	 * validateCsarMeta();
	 * 
	 * validateToscaMeta();
	 *
	 * validateManifest();
	 * }
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

	private static boolean csarExtract(String filePath) {

		try {
			String tempfolder = CsarUtil.getUnzipDir(filePath);
			csarFiles = CsarUtil.unzip(filePath, tempfolder);

		} catch (IOException e1) {
			LOG.error("CSAR extraction error ! " + e1.getMessage());

			return false;
		}
		return true;
	}

	public static boolean validateCsarMeta() {

		String cfile = csarFiles.get(CommonConstants.CSAR_META);
		if (!cfile.isEmpty()) {
				File file = new File(cfile);
				BufferedReader reader = null;

				try {
					reader = new BufferedReader(new FileReader(file));
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
					LOG.error("Exception cought while validateCsarMeta ! " + e2.getMessage());
					//e2.printStackTrace();

				} finally {
					if (reader != null) {
						try {
							reader.close();

						} catch (IOException e1) {
							LOG.error("close reader failed ! " + e1.getMessage());
						}
					}

				}
		}

		return false;
	}


	public static boolean validateToscaMeta() {

        String cfile = csarFiles.get(CommonConstants.TOSCA_META);
        try {
            if (!cfile.isEmpty() && cfile.contains( System.getProperty("file.separator")+ CommonConstants.TOSCA_METADATA + System.getProperty("file.separator") + CommonConstants.TOSCA_META)) {

                String value = checkEntryFor("Entry-Definitions:", cfile);
                if (value == null) {
                    return false;
                    //Check if Entry-Defintions pointed file exists in CSAR
                } else if (csarFiles.get(value) != null) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOG.error("Could not read file %s ! " + e.getMessage(), cfile);
        }

        return false;
    }


	private static String checkEntryFor(String attribute, String fileWithPath) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(fileWithPath), UTF_8);

        for(String strLine : lines) {
            if (!attribute.isEmpty() && strLine.contains(attribute)) {
                return strLine.substring(attribute.length(), strLine.length()).trim();
            }
        }
        return null;
    }
}

