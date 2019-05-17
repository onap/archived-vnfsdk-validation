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

import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.VTPValidateCSAR;
import org.onap.cvc.csar.cc.VTPValidateCSARBase;
import org.yaml.snakeyaml.Yaml;


@OnapCommandSchema(schema = "vsp-compliance-check-compute-flavor.yaml")
public class VSPComplianceCheckComputeFlavor extends VTPValidateCSARBase {

    public static class CSARErrorEntryMissmatchComputeFlavor extends CSARArchive.CSARErrorMismatch {
        public CSARErrorEntryMissmatchComputeFlavor(String entry, String file, String message, String expected, String acutal) {
            super(entry, file, -1, message, expected, acutal);
            this.setCode("0x1000");
        }
    }

    private static Properties prp = new Properties();

    private static List<Flavor> validFlavors = new ArrayList<>();

    static {
        try {
            prp.load(VTPValidateCSAR.class.getClass().getResourceAsStream("/vsp_compliance_check.properties"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static List<Flavor> findCompliantFlavors() {
        List<Flavor> flavorList = new ArrayList<Flavor>();

        Flavor flavor = null;

        List<String> configFileList1 = new ArrayList<>();
        List<String> configFileList2 = new ArrayList<>();

        int i = 1;
        while (true) {
            String cpu = prp.getProperty("compute.flavor.check." + i + ".cpu");
            String mem = prp.getProperty("compute.flavor.check." + i + ".memory");
            String disk = prp.getProperty("compute.flavor.check." + i + ".disk");

            if (cpu != null && mem != null && disk != null) {
                Flavor f = new Flavor();
                f.setCpu(Integer.parseInt(cpu));
                f.setMemorySize(Integer.parseInt(mem));
                f.setDiskSize(Integer.parseInt(disk));
                flavorList.add(f);
            } else if (cpu == null && mem == null && disk == null) {
                break;
            } else {
                LOG.info("Some Flavor details is missing in configuration :" + i );
            }

            i++;
        }

        return flavorList;
    }

    @Override
    protected String getVnfReqsNo() {
        return "vsp-comliance-check-compute-flavor";
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


    @Override
    protected void validateCSAR(CSARArchive csar) throws Exception {

        List<Flavor> vspFlavors = findCompliantFlavors();

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
                        flavorYaml = new VSPComplianceCheckComputeFlavor.Flavor();
                        flavorYaml.setCpu(virtualCPU);
                        flavorYaml.setMemorySize(memSize);
                        flavorYaml.setDiskSize(dSize);
                        nodeWithFlavor.put(nodeName, flavorYaml);
                    }
                }
            }

            //check the flavors against the compliant flavors
            for (Map.Entry<String, Flavor> entry: nodeWithFlavor.entrySet()) {
                String nodeName = entry.getKey();
                Flavor flavor = entry.getValue();

                if (!vspFlavors.contains(flavor)) {
                   this.errors.add(new CSARErrorEntryMissmatchComputeFlavor(
                           nodeName,
                           csar.getDefinitionYamlFile().getName(), "Compute Flavor does not match with VSP compliant flavors", vspFlavors.toString(), flavor.toString()
                    ));
                }
            }
        }
    }
}
