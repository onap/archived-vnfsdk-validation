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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.FileArchive;
import org.onap.cvc.csar.PnfCSARArchive;
import org.onap.cvc.csar.VTPValidateCSAR;
import org.onap.cvc.csar.ZipFileContentValidator;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


@OnapCommandSchema(schema = "vsp-compliance-check-compute-flavor-report.yaml")
public class VSPComplianceCheckComputeFlavorReport extends OnapCommand {

    public static class CSARErrorEntryMissmatchComputeFlavor extends CSARArchive.CSARErrorMismatch {
        public CSARErrorEntryMissmatchComputeFlavor(String entry, String file, String message, String expected, String acutal) {
            super(entry, file, -1, message, expected, acutal);
            this.setCode("0x1000");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARBase.class);

    private final ZipFileContentValidator zipFileContentValidator = new ZipFileContentValidator();

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();
        boolean isPnf = isPnf();

        //execute
        try (CSARArchive csar = isPnf ? new PnfCSARArchive(): new CSARArchive()){
            csar.init(path);
            csar.parse();

            for (Map.Entry<String, Flavor> entry: this.findFlavors(csar).entrySet()) {
                String nodeName = entry.getKey();
                Flavor flavor = entry.getValue();
                this.getResult().getRecordsMap().get("node").getValues().add(nodeName);
                this.getResult().getRecordsMap().get("cpu").getValues().add(Integer.toString(flavor.cpu));
                this.getResult().getRecordsMap().get("memory").getValues().add(Integer.toString(flavor.memorySize));
                this.getResult().getRecordsMap().get("disk").getValues().add(Integer.toString(flavor.diskSize));                
            }
        } catch (Exception e) {
            LOG.error("Failed to find the flavor details from CSAR" , e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }
   }

    private boolean isPnf() {
        final OnapCommandParameter pnf = getParametersMap().get("pnf");
        return pnf != null && (boolean) pnf.getValue();
    }

    public static class Flavor {
        private int cpu;
        private int memorySize;
        private int diskSize;

        public void setCpu(int cpu) {
            this.cpu = cpu;
        }

        public void setDiskSize(int diskSize) {
            this.diskSize = diskSize;
        }

        public void setMemorySize(int memorySize) {
            this.memorySize = memorySize;
        }

        @Override
        public String toString() {
            return "CPU: " + cpu + ", Memory: " + memorySize + ", Disk: " + diskSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Flavor flavor = (Flavor) o;
            return cpu == flavor.cpu &&
                    memorySize == flavor.memorySize &&
                    diskSize == flavor.diskSize;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cpu, memorySize, diskSize);
        }
    }

    private  Map<String, Flavor> findFlavors(CSARArchive csar) throws Exception {
        Map<String, Flavor> nodeWithFlavor = new HashMap<String, Flavor>();

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

                    if (type.equalsIgnoreCase("tosca.nodes.nfv.vdu.compute")) {
                        //Improve this logic.
                        Map virtual_compute = (Map) ((Map) ((Map) node.get("capabilities")).get("virtual_compute")).get("properties");
                        int virtualCPU = (int) ((Map) virtual_compute.get("virtual_cpu")).get("num_virtual_cpu");

                        String memorySize = (String) ((Map) virtual_compute.get("virtual_memory")).get("virtual_mem_size");
                        int memSize = Integer.valueOf(memorySize.replaceAll("[^0-9]", ""));

                        if (node.get("requirements") instanceof List) {
                            List<?> list = (List<?>) node.get("requirements");
                            Map disk = (Map) list.get(0);
                            List VSList = new ArrayList(disk.values());
                            Map nodeforLBU = null;

                            for (int i = 0; i < VSList.size(); i++) {
                                nodeforLBU = (Map) VSList.get(i);
                            }
                            Object objForNode = nodeforLBU.get("node");
                            if (nodeTmpls.containsKey(objForNode)) {
                                Map<String, ?> mapForNextNode = (Map<String, ?>) nodeTmpls.get(objForNode);
                                if (mapForNextNode.containsKey("type")) {
                                    type = (String) mapForNextNode.get("type");
                                    if (type.equalsIgnoreCase("tosca.nodes.nfv.Vdu.VirtualStorage")) {

                                        String diskSize = (String) ((Map) mapForNextNode.get("properties")).get("size_of_storage");
                                        dSize = Integer.valueOf(diskSize.replaceAll("[^0-9]", ""));
                                    }
                                }
                            }
                        }

                        Flavor flavorYaml = new Flavor();
                        flavorYaml = new VSPComplianceCheckComputeFlavorReport.Flavor();
                        flavorYaml.setCpu(virtualCPU);
                        flavorYaml.setMemorySize(memSize);
                        flavorYaml.setDiskSize(dSize);
                        nodeWithFlavor.put(nodeName, flavorYaml);
                    }
                }
            }

            return nodeWithFlavor;
        }
    }
}
