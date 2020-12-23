/*
 * Copyright 2020 Nokia
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

package org.onap.functional.util;


import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.output.OnapCommandResult;
import org.onap.cli.main.OnapCli;

public class OnapCliWrapper extends OnapCli {

    private OnapCommandResult commandResult;

    public OnapCliWrapper(String[] args) {
        super(args);
    }

    @Override
    public void handleTracking(OnapCommand cmd) throws OnapCommandException {
        super.handleTracking(cmd);
        this.commandResult = cmd.getResult();
    }

    public OnapCommandResult getCommandResult() {
        return this.commandResult;
    }
}
