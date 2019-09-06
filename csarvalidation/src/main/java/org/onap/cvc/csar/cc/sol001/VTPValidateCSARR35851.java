/*
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

package org.onap.cvc.csar.cc.sol001;

import java.io.FileInputStream;
import java.util.Map;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.yaml.snakeyaml.Yaml;

@OnapCommandSchema(schema = "vtp-validate-csar-r35851.yaml")
public class VTPValidateCSARR35851 extends VTPValidateCSARBase {

    private static final String SET_CODE = "0x1000";

    public static class CSARErrorEntryMissingDefinitionYamlVnfVirtualLink extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingDefinitionYamlVnfVirtualLink(String defYaml, String entry) {
            super(entry, defYaml);
            this.setCode(SET_CODE);
        }
    }


    public static class CSARErrorEntryMissingDefinitionYamlVduCp extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingDefinitionYamlVduCp(String defYaml, String entry) {
            super(entry, defYaml);
            this.setCode(SET_CODE);
        }
    }

    public static class CSARErrorEntryMissingDefinitionYamlVnfExtCp extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingDefinitionYamlVnfExtCp(String defYaml, String entry) {
            super(entry, defYaml);
            this.setCode(SET_CODE);
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        try(FileInputStream ipStream = new FileInputStream(csar.getDefinitionYamlFile())) {
            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);
            yaml = (Map<String, ?>) yaml.get("topology_template");
            Map<String, ?> nodeTmpls = (Map<String,?>) yaml.get("node_templates");

            boolean []vlExist = new boolean[3];

            for (Object nodeO: nodeTmpls.values()) {
                Map<String, ?> node = (Map<String, ?>) nodeO;
                if (node.containsKey("type")) {
                    String type = (String)node.get("type");
                    if ("tosca.nodes.nfv.VnfVirtualLink".equalsIgnoreCase(type)) {
                        vlExist[0] = true;

                    }

                    if ("tosca.nodes.nfv.VduCp".equalsIgnoreCase(type)) {
                        vlExist[1] = true;

                    }

                    if ("tosca.nodes.nfv.VnfExtCp".equalsIgnoreCase(type)) {
                        vlExist[2] = true;

                    }
                }
            }

            if (!vlExist[0])
                this.errors.add(new CSARErrorEntryMissingDefinitionYamlVnfVirtualLink(
                    csar.getDefinitionYamlFile().getName(),
                    "nodes VnfVirtualLink"));


            if (!vlExist[1])
                this.errors.add(new CSARErrorEntryMissingDefinitionYamlVduCp(
                        csar.getDefinitionYamlFile().getName(),
                        "nodes VduCp"));

            if (!vlExist[2])
                this.errors.add(new CSARErrorEntryMissingDefinitionYamlVnfExtCp(
                        csar.getDefinitionYamlFile().getName(),
                        "nodes VnfExtCp"));
        }

    }

    @Override
    protected String getVnfReqsNo() {
        return "R35851";
    }
}
