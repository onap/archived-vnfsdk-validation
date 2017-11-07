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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


public class ValidatorSchemaLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ValidatorSchemaLoader.class);

    // Map of Schema files
    private Map<String, ?> toscaMeta;
    private Map<String, ?> csarentryd;
    private Map<String, ?> mrfYaml;
    private Map<String, ?> mrfManifest;

    // List of configured schemas
    static List<String> schemaFileList =  new ArrayList<String>();

    // SOL004 rule files
    static HashMap<String, String> optionOneSchema;
    static HashMap<String, String> optionTwoSchema;

    private String schema_folder;

    public ValidatorSchemaLoader() throws NullPointerException, FileNotFoundException, ScannerException, IOException {
        try {
            loadResources();
        } catch (Exception e1) {
            LOG.error("Schema file not found or schema repository corrupted", e1);
        }
    }

    @SuppressWarnings("unchecked")
	private boolean loadResources() throws FileNotFoundException, URISyntaxException {
        String path = FileUtil.getResourceFilePath("../../../../schema", getClass());
        try (Stream<Path> paths = Files.walk(Paths.get(path))){
            paths.filter(Files::isRegularFile)
                 .forEach((Path e) -> {
                        File file = e.toFile();
                        if (!file.isDirectory() && (
                                FilenameUtils.isExtension(file.getName(), "yaml") ||
                                        FilenameUtils.isExtension(file.getName(), "mf") ||
                                        FilenameUtils.isExtension(file.getName(), "meta"))) {

                            Yaml yaml = new Yaml();
                            FileInputStream inputStream = null;
                            try {
                                inputStream = new FileInputStream(file);
                                switch (file.getName()) {
                                    case "TOSCA.meta":
                                        toscaMeta = (Map<String, ?>) yaml.load(inputStream);
                                        break;
                                    case "CSAR.meta":
                                        csarentryd = (Map<String, ?>) yaml.load(inputStream);
                                        break;
                                    case "MRF.yaml":
                                        mrfYaml = (Map<String, ?>) yaml.load(inputStream);
                                        break;
                                    case "MRF.mf":
                                        mrfManifest = (Map<String, ?>) yaml.load(inputStream);
                                        break;
                                }
                            }catch (ScannerException | FileNotFoundException e2){
                                LOG.error("Schema files %s format is not as per standard prescribed", file.getName(), e2);
                            }finally {
                                FileUtil.closeFileStream(inputStream);
                            }
                        }
                        schemaFileList.add(e.toAbsolutePath().toString());
                    });
            
        } catch (NullPointerException | IOException err) {
            LOG.error("Schema files/folder access error"+err);
        }

        return true;
    }

    public Map<String, ?> getToscaMeta() {
        return toscaMeta;
    }

    public Map<String, ?> getCsarentryd() {
        return csarentryd;
    }

    public Map<String, ?> getMrfYaml() {
        return mrfYaml;
    }

    public Map<String, ?> getMrfManifest() {
        return mrfManifest;
    }

    public List<String> getSchemaFileList() {
        return schemaFileList;
    }
}