/**
 * Copyright 2019 Huawei Technologies Co., Ltd.
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

package org.onap.cvc.csar.vspcc;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.VTPValidateCSAR;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.yaml.snakeyaml.Yaml;


@OnapCommandSchema(schema = "vsp-compliance-check-sriov-flavor.yaml")
public class VSPComplianceCheckSRIOVFlavor extends VTPValidateCSARBase {

    public static class CSARErrorInvalidEntrySRIOVFlavorCheckError extends CSARArchive.CSARErrorInvalidEntry {
        public CSARErrorInvalidEntrySRIOVFlavorCheckError(String entry, String file, String message) {
            super(entry, file, -1, message);
            this.setCode("0x1000");
        }
    }

    private static Properties prp = new Properties();


    static {
        try {
            prp.load(VTPValidateCSAR.class.getClass().getResourceAsStream("/vsp_compliance_check.properties"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "vsp-comliance-check-sriov-flavor";
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        Boolean sriovShouldExist = Boolean.valueOf(prp.getProperty("sriov.flavor.check.exist", "false"));

        try (FileInputStream ipStream = new FileInputStream(csar.getDefinitionYamlFile())) {
            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);
            yaml = (Map<String, ?>) yaml.get("topology_template");
            Map<String, ?> nodeTmpls = (Map<String, ?>) yaml.get("node_templates");

            for (Map.Entry<String, ?> nodeEntry : nodeTmpls.entrySet()) {
                int dSize = 0;
                Object nodeO = nodeEntry.getValue();
                String nodeName = nodeEntry.getKey();

                Map<String, ?> node = (Map<String, ?>) nodeO;
                if (node.containsKey("type")) {
                    String type = (String) node.get("type");

                    if (type.equalsIgnoreCase("tosca.nodes.nfv.vducp")) {
                        //Improve this logic.
                        Map <String, ? > properties = (Map<String, ? >) node.get("properties");
                        boolean exist = properties.containsKey("virtual_network_interface_requirements");

                        if ((sriovShouldExist && !exist) || (!sriovShouldExist && exist)) {
                            this.errors.add(new CSARErrorInvalidEntrySRIOVFlavorCheckError(
                                    nodeName,
                                    csar.getDefinitionYamlFile().getName(), "SRIOV Flavor should " + (sriovShouldExist ? "": " not ") + "exist"
                             ));
                        }
                    }
                }
            }
        }
    }
}
