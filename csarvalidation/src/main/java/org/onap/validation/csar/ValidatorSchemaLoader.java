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


import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


public class ValidatorSchemaLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ValidatorSchemaLoader.class);

    // Map of Schema files
    private static Map<String, ?> toscaMeta;
    private static Map<String, ?> csarentryd;
    private static Map<String, ?> mrfYaml;
    private static Map<String, ?> mrfManifest;

    // List of configured schemas
    static List<String> schemaFileList =  new ArrayList<String>();

    // SOL004 rule files
    static HashMap<String, String> optionOneSchema;
    static HashMap<String, String> optionTwoSchema;

	private String schema_folder;
    public ValidatorSchemaLoader() throws Exception {


                    try {
                            loadResources();
                    } catch ( FileNotFoundException e1) {
                        LOG.error("Schema file not found or schema repository corrupted");

                    }
    }


    @SuppressWarnings("unchecked")
	private boolean loadResources() throws FileNotFoundException {
    	
        try {
            String schema_folder = getClass().getResource("../../../../schema").getPath();

            Files.walk(Paths.get(schema_folder))
                 .filter(Files::isRegularFile)
                 .forEach((Path e) -> {

                        File file = e.toFile();

                        if (!file.isDirectory() && (
                                FilenameUtils.isExtension(file.getName(), "yaml") ||
                                        FilenameUtils.isExtension(file.getName(), "mf") ||
                                        FilenameUtils.isExtension(file.getName(), "meta"))) {

                            Yaml yaml = new Yaml();

                            switch (file.getName()) {
                                case "TOSCA.meta":
                                    try {
                                        toscaMeta = (Map<String, ?>) yaml.load(new FileInputStream(file));
                                    } catch (ScannerException | FileNotFoundException e1) {
                                        LOG.error("Schema files %s format is not as per standard prescribed", file.getName());
                                    }
                                    break;
                                case "CSAR.meta":
                                    try {
                                        csarentryd = (Map<String, ?>) yaml.load(new FileInputStream(file));
                                    } catch (ScannerException | FileNotFoundException e2) {
                                        LOG.error("Schema files %s format is not as per standard prescribed", file.getName());
                                    }
                                    break;
                                case "MRF.yaml":
                                    try {
                                        mrfYaml = (Map<String, ?>) yaml.load(new FileInputStream(file));
                                    } catch (ScannerException | FileNotFoundException e2) {
                                        LOG.error("Schema files %s format is not as per standard prescribed", file.getName());
                                    }
                                    break;
                                case "MRF.mf":
                                    try {
                                        mrfManifest = (Map<String, ?>) yaml.load(new FileInputStream(file));
                                    } catch (ScannerException | FileNotFoundException e2) {
                                        LOG.error("Schema files %s format is not as per standard prescribed", file.getName());
                                    }
                                    break;
                            }
                        }
                        schemaFileList.add(e.toAbsolutePath().toString());
                    });
            
        } catch (NullPointerException | IOException err) {
            LOG.error("Schema files/folder access error"+err);
        }

        return true;
    }

    public static Map<String, ?> getToscaMeta() {
        return toscaMeta;
    }

    public static Map<String, ?> getCsarentryd() {
        return csarentryd;
    }

    public static Map<String, ?> getMrfYaml() {
        return mrfYaml;
    }

    public static Map<String, ?> getMrfManifest() {
        return mrfManifest;
    }

    public static List<String> getSchemaFileList() {
        return schemaFileList;
    }
}