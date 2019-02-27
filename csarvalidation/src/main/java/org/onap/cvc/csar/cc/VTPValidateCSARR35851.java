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

package org.onap.cvc.csar.cc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandExecutionFailed;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.CSARArchive.CSARError;
import org.onap.cvc.csar.CSARArchive.CSARErrorEntryMissing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * R-35851: The VNF Package MUST include VNF topology that describes
 *  basic network and application connectivity internal and external to the VNF
 *  including Link type, KPIs, Bandwidth, latency, jitter, QoS
 *  (if applicable) for each interface
 */
@OnapCommandSchema(schema = "vtp-validate-csar-r35851.yaml")
public class VTPValidateCSARR35851 extends OnapCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VTPValidateCSARR35851.class);

    public static class CSARErrorEntryMissingDefinitionYamlTopology extends CSARErrorEntryMissing {
        public CSARErrorEntryMissingDefinitionYamlTopology(String defYaml, String entry) {
            super(
                    entry,
                    defYaml,
                    -1,
                    "The VNF Package MUST include VNF topology that describes basic "
                    + "network and application connectivity internal and external to "
                    + "the VNF including Link type, KPIs, Bandwidth, latency, jitter, "
                    + "QoS (if applicable) for each interface");
            this.setSubCode("r35851-0x1000");
        }
    }

    private List<CSARError> validate(CSARArchive csar) throws FileNotFoundException, IOException {
        List<CSARError> errors = new ArrayList<>();

        try(FileInputStream ipStream = new FileInputStream(csar.getDefinitionYamlFile())) {
            Map<String, ?> yaml = (Map<String, ?>) new Yaml().load(ipStream);
            yaml = (Map<String, ?>) yaml.get("topology_template");
            Map<String, ?> nodeTmpls = (Map<String,?>) yaml.get("node_templates");

            boolean vlExist = false;

            for (Object nodeO: nodeTmpls.values()) {
                Map<String, ?> node = (Map<String, ?>) nodeO;
                if (node.containsKey("type")) {
                    String type = (String)node.get("type");
                    if (type.equalsIgnoreCase("tosca.nodes.nfv.VnfVirtualLink")) {
                        vlExist = true;
                        break;
                    }
                }
            }

            if (!vlExist)
            errors.add(new CSARErrorEntryMissingDefinitionYamlTopology(
                    csar.getDefinitionYamlFile().getName(),
                    "Topology VL"));
        }

        return errors;
    }

    @Override
    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();
        List<CSARError> errors = new ArrayList<>();
        //execute
        try {
            CSARArchive csar = new CSARArchive();
            csar.init(path);
            csar.parse();

               errors = this.validate(csar);

            csar.cleanup();
        } catch (Exception e) {
            LOG.error("R-35851: ", e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }

        this.getResult().setOutput(errors);

        //set the result
        for (CSARError e: errors) {
            this.getResult().getRecordsMap().get("code").getValues().add(e.getCode());
            this.getResult().getRecordsMap().get("message").getValues().add(e.getMessage());
            this.getResult().getRecordsMap().get("file").getValues().add(e.getFile());
            this.getResult().getRecordsMap().get("line-no").getValues().add(Integer.toString(e.getLineNumber()));
        }
   }
}
