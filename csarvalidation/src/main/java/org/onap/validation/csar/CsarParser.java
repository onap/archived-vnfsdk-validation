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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import java.io.File;
import java.io.FileOutputStream;

public class CsarParser {

	private static final Logger LOG = LoggerFactory.getLogger(CsarParser.class);

	
	
	public static boolean csarExtract(String filePath) {

		try {
			String tempfolder = CsarUtil.getUnzipDir(filePath);
			CsarUtil.csarFiles = CsarUtil.unzip(filePath, tempfolder);
		} catch (IOException e1) {
			LOG.error("CSAR extraction error ! " + e1.getMessage());
			return false;
		}
		return true;
	}

	
	public static boolean validateCsarMeta() {

		for (String cfile : CsarUtil.csarFiles) {
			if (cfile.endsWith(CommonConstants.CSAR_META)) {
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
					}
					reader.close();
				} catch (IOException e2) {
					e2.printStackTrace();
					return false;
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e1) {
							LOG.error("close reader failed ! " + e1.getMessage());
						}
					}
				}
				return true;
			}

		}

		return false;
	}
}
