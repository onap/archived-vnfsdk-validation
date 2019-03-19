/*
 * Copyright 2019 Nokia
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
 *
 */

package org.onap.cvc.csar;

public class PnfCSARError extends CSARArchive.CSARError {
    public PnfCSARError(String errorCode, String message,  int lineNumber, String file) {
        super(errorCode);
        this.message = message;
        this.file = file;
        this.lineNumber = lineNumber;
    }

    public static class PnfCSARErrorInvalidEntry extends PnfCSARError {
        public PnfCSARErrorInvalidEntry(String entry, String file, int lineNumber) {
            super("0x2000", "Invalid. Entry [" + entry + "]", lineNumber, file);
        }
    }

    public static class PnfCSARErrorWarning extends PnfCSARError {
        public PnfCSARErrorWarning(String entry, String file, int lineNumber) {
            super("0x2001", "Warning. Entry [" + entry + "]", lineNumber, file);
        }
    }

    public static class PnfCSARErrorEntryMissing extends PnfCSARError {
        public PnfCSARErrorEntryMissing(String entry, String file, int lineNumber) {
            super("0x2002", "Missing. Entry [" + entry + "]", lineNumber, file);
        }
    }
}


