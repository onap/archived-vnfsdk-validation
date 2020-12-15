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
package org.onap.validation.yaml.util;

import java.util.ArrayList;
import java.util.List;

public class Args {

    private final List<String> data;

    public Args(List<String> data) {
        this.data = new ArrayList<>(data);
    }

    public String getArg(int index) {
        if (this.data.size() <= index) {
            throw new IllegalArgumentException(String.format("Argument with index %d is not available!", index));
        }
        return this.data.get(index);
    }
}
