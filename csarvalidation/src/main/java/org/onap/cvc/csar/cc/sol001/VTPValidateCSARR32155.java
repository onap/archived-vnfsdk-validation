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

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;

@OnapCommandSchema(schema = "vtp-validate-csar-r32155.yaml")
public class VTPValidateCSARR32155 extends VTPValidateCSARBase {

    public static class CSARErrorEntryMissingVirtualLink extends CSARArchive.CSARErrorEntryMissing {
        public CSARErrorEntryMissingVirtualLink(String entry, String file) {
            super(entry, file);
            this.setCode("0x1000");
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {

        try (FileInputStream ipStream = new FileInputStream(csar.getDefinitionYamlFile())) {

            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);
            yaml = (Map<String, ?>) yaml.get("topology_template");
            Map<String, ?> yamlTmpls = (Map<String, ?>) yaml.get("node_templates");

            boolean vlExist[] = new boolean[2];

            for (Object node0 : yamlTmpls.values()) {

                Map<String, ?> node = (Map<String, ?>) node0;

                if (node.containsKey("type")) {
                    String type = (String) node.get("type");

                    if (type.equalsIgnoreCase("tosca.nodes.nfv.VnfVirtualLinkDesc")) {

                        vlExist[0] = true;
                    }

                    if (type.equalsIgnoreCase("tosca.capabilities.nfv.VirtualLinkable")) {

                        vlExist[1] = true;
                    }
                }
            }

            if (!vlExist[0]) {
                this.errors.add(
                        new CSARErrorEntryMissingVirtualLink(
                                "Virtual",
                                csar.getDefinitionYamlFile().getName()));
            }

            if (!vlExist[1]) {
                this.errors.add(
                        new CSARErrorEntryMissingVirtualLink(
                                "Virtual",
                                csar.getDefinitionYamlFile().getName()));
            }
        }

    }

    @Override
    protected String getVnfReqsNo() {
        return "R32155";
    }

}
