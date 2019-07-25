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

import org.apache.commons.lang3.tuple.Pair;

public class ManifestLine {

    private final String line;

    private ManifestLine(String line) {
        this.line = line;
    }

    public static ManifestLine of(String line) {
        return new ManifestLine(line);
    }

    public Pair<String, String> parse() {
        String[] elements = line.split(": ");
        if (elements.length == 2) {
            return Pair.of(elements[0], elements[1]);
        }

        if (line.endsWith(":")) {
            return Pair.of(line.substring(0, line.length() - 1), "");
        } else {
            return Pair.of(line, "");
        }


    }

    boolean startsWith(String word) {
        return line.trim().toLowerCase().startsWith(word);
    }

    boolean isEmpty() {
        return line.trim().isEmpty();
    }

}
