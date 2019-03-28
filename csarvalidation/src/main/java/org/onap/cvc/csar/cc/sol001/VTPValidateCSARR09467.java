/*
x * Copyright 2017 Huawei Technologies Co., Ltd.
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

@OnapCommandSchema(schema = "vtp-validate-csar-r09467.yaml")
public class VTPValidateCSARR09467 extends VTPValidateCSARBase {

    public static class CSARErrorEntryMissingVDUCompute extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingVDUCompute(String defYaml, String entry) {
            super(entry, defYaml);
            this.setCode("0x1000");
        }
    }

    public static class CSARErrorEntryMissingVDUVirtualStorage extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingVDUVirtualStorage(String defYaml, String entry) {
            super(entry, defYaml);
            this.setCode("0x1001");
        }
    }

    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {
        try(FileInputStream ipStream = new FileInputStream(csar.getDefinitionYamlFile())) {
            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);
            yaml = (Map<String, ?>) yaml.get("topology_template");
            Map<String, ?> nodeTmpls = (Map<String,?>) yaml.get("node_templates");

            boolean computeExist = false;
            boolean storageExist = false;

            for (Object nodeO: nodeTmpls.values()) {
                Map<String, ?> node = (Map<String, ?>) nodeO;
                if (node.containsKey("type")) {
                    String type = (String)node.get("type");
                    if (type.equalsIgnoreCase("tosca.nodes.nfv.VDU.Compute")) {
                        computeExist = true;

                    }

                    if (type.equalsIgnoreCase("tosca.nodes.nfv.VDU.VirtualStorage")) {
                        storageExist = true;

                    }
                }
            }

            if (!computeExist)
                this.errors.add(new CSARErrorEntryMissingVDUCompute(
                    csar.getDefinitionYamlFile().getName(),
                    "VDU Compute"));

            if (!storageExist)
                this.errors.add(new CSARErrorEntryMissingVDUVirtualStorage(
                        csar.getDefinitionYamlFile().getName(),
                        "VirtualStorage"));
        }
    }

    @Override
    protected String getVnfReqsNo() {
        return "R09467";
    }
}
