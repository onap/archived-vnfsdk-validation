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
import java.net.URISyntaxException;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class ValidatorSchemaLoader {

    private static final String FILE_NAME = "/schema/";

    private static final Logger LOG = LoggerFactory.getLogger(ValidatorSchemaLoader.class);

    // Map of Schema files
    private Map<String, ?> toscaMeta;

    private Map<String, ?> csarentryd;

    private Map<String, ?> mrfYaml;

    private Map<String, ?> mrfManifest;

    // List of configured schemas
    static List<String> schemaFileList = new ArrayList<>();

    // SOL004 rule files
    static HashMap<String, String> optionOneSchema;

    static HashMap<String, String> optionTwoSchema;

    public ValidatorSchemaLoader() {

        try {
            loadResources();
        } catch(FileNotFoundException e1) {
            LOG.error("Schema file not found or schema repository corrupted", e1);

        } catch(URISyntaxException e) {
            // TODO Auto-generated catch block
            LOG.error("Illegal character in query at index", e);
        }
    }

    private Map<String, ?> readYaml(String fileName) {
        Yaml yaml = new Yaml();
           return (Map<String, ?>)yaml.load(this.getClass().getResourceAsStream(fileName));
    }

    @SuppressWarnings("unchecked")
    private boolean loadResources() throws FileNotFoundException, URISyntaxException {
        for (String metaFile: new String []{"TOSCA.meta", "CSAR.meta", "MRF.mf" }) {
            switch(metaFile) {
                case "TOSCA.meta":
                     toscaMeta = this.readYaml(FILE_NAME + metaFile);
                    break;
                case "CSAR.meta":
                    csarentryd = this.readYaml(FILE_NAME + metaFile);
                    break;
                case "MRF.yaml":
                    mrfYaml = this.readYaml(FILE_NAME + metaFile);
                    break;
                default:
                    mrfManifest = this.readYaml(FILE_NAME + metaFile);
                    break;
            }
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
}
