/*
 * Copyright 2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.onap.validation.cli;

import org.onap.validation.cli.command.validate.ResponseStorage;
import org.onap.validation.cli.command.validate.ToJsonConverter;
import org.onap.validation.cli.command.validate.ValidatePmDictionaryYamlCommand;
import org.onap.validation.cli.core.Cli;
import org.onap.validation.cli.core.Console;
import org.onap.validation.yaml.util.Args;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        final Console console = new Console();
        final ResponseStorage storage = new ResponseStorage();

        System.exit(
                run(args, console, storage)
        );
    }

    static int run(String[] args, Console console, ResponseStorage responseStorage) {

        Cli<String> cli = new Cli<>(console, responseStorage);

        return cli.run(
                new Args(List.of(args)),
                new ValidatePmDictionaryYamlCommand(new ToJsonConverter())
        );
    }
}
