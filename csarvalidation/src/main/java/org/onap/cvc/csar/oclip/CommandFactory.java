/*
 * Copyright 2021 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.cvc.csar.oclip;

import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.error.OnapCommandInvalidParameterValue;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.registrar.OnapCommandRegistrar;


public class CommandFactory {

    private static final String PNF_ATTRIBUTE_NAME = "pnf";
    private static final String COMMAND_PREFIX = "csar-validate-";
    private static final String CSAR_PARAM_NAME = "csar";

    public Command createForVnf(String req, String pathToCsarFile, String product) throws OnapCommandException {
        OnapCommand cmd = createCommand(req, pathToCsarFile, product);
        return new Command(cmd);
    }

    public Command createForPnf(String req, String pathToCsarFile, String product) throws OnapCommandException {
        OnapCommand cmd = createCommand(req, pathToCsarFile, product);
        markCommandAsPnf(cmd);
        return new Command(cmd);
    }

    private OnapCommand createCommand(String req, String pathToCsarFile, String product) throws OnapCommandException {
        String command = createCommandFor(req);
        OnapCommand cmd = OnapCommandRegistrar.getRegistrar().get(command, product);
        setPathToCsar(pathToCsarFile, cmd);
        return cmd;
    }

    private String createCommandFor(String req) {
        return COMMAND_PREFIX + req;
    }

    private void setPathToCsar(String pathToCsarFile, OnapCommand cmd) throws OnapCommandInvalidParameterValue {
        cmd.getParametersMap().get(CSAR_PARAM_NAME).setValue(pathToCsarFile);
    }

    private void markCommandAsPnf(OnapCommand cmd) throws OnapCommandInvalidParameterValue {
        final OnapCommandParameter pnf = cmd.getParametersMap().get(PNF_ATTRIBUTE_NAME);
        pnf.setValue(true);
    }
}
