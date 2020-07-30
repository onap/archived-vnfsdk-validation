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

public class ValidatorSchemaLoader {

    private static final String FILE_NAME = "/schema/";

    private static final Logger LOG = LoggerFactory.getLogger(ValidatorSchemaLoader.class);

    // Map of Schema files
    private Map<String, Object> toscaMeta;

    private Map<String, Object> csarentryd;

    private Map<String, Object> mrfYaml;

    private Map<String, Object> mrfManifest;

    // List of configured schemas
    static List<String> schemaFileList = new ArrayList<>();

    // SOL004 rule files
    static HashMap<String, String> optionOneSchema;

    static HashMap<String, String> optionTwoSchema;

    private String schemaFolder;

    public ValidatorSchemaLoader() {

            loadResources();
    }

    private Map<String, Object> readYaml(String fileName) {
        Yaml yaml = new Yaml();
           return (Map<String, Object>)yaml.load(this.getClass().getResourceAsStream(fileName));
    }

    @SuppressWarnings("unchecked")
    private boolean loadResources() {
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

    public Map<String, Object> getToscaMeta() {
        return toscaMeta;
    }

    public Map<String, Object> getCsarentryd() {
        return csarentryd;
    }

    public Map<String, Object> getMrfYaml() {
        return mrfYaml;
    }

    public Map<String, Object> getMrfManifest() {
        return mrfManifest;
    }
}
