/*
 *Copyright 2020 Nokia
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
package org.onap.validation.cli.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console {

    public static final Logger logger = LoggerFactory.getLogger(Console.class);

    public void info(String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    public void error(Exception ex) {
        System.err.println(ex.getMessage() + ", more information in log file.");
        logger.error("Internal error", ex);
    }

    public void error(String msg) {
        System.err.println(msg);
        logger.error(msg);
    }

}
