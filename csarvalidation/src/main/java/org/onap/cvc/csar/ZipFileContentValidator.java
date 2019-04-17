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

import java.util.ArrayList;
import java.util.List;

public class ZipFileContentValidator {

    public static class CSARErrorCertMissing extends CSARArchive.CSARError {
        CSARErrorCertMissing() {
            super("0x1008");
            this.message = "Missing. Cert file is not available!";
        }
    }

    public static class CSARErrorCMSMissing extends CSARArchive.CSARError {
        CSARErrorCMSMissing() {
            super("0x1009");
            this.message = "Missing. CMS file is not available!";
        }
    }

    public List<CSARArchive.CSARError> validate(FileArchive.Workspace workspace){
        final ArrayList<CSARArchive.CSARError> retValue = new ArrayList<>();

        if(!workspace.getPathToCertFile().isPresent()){
            retValue.add(new CSARErrorCertMissing());
        }

        if(!workspace.getPathToCmsFile().isPresent()){
            retValue.add(new CSARErrorCMSMissing());
        }

        return retValue;
    }
}
