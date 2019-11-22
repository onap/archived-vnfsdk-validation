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

package org.onap.cvc.csar.parser;

import java.util.Collections;
import java.util.List;

public class ManifestFileModel {
    private final List<String> data;
    private final List<String> cms;
    private final String newLine;

    public ManifestFileModel(List<String> data, List<String> cms) {
        this(data, cms, "\n");
    }

    public ManifestFileModel(List<String> data, List<String> cms, String newLine) {
        this.data = data;
        this.cms = cms;
        this.newLine = newLine;
    }

    public List<String> getData() {
        return Collections.unmodifiableList(data);
    }

    public List<String> getCMS() {
        return Collections.unmodifiableList(cms);
    }

    public String getNewLine() {
        return newLine;
    }
}
