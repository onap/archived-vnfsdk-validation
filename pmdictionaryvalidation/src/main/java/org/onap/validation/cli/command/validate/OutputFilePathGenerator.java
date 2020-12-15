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
package org.onap.validation.cli.command.validate;

import java.nio.file.Path;

public class OutputFilePathGenerator {

    public static final int FILE_NAME_ABBREVIATION_INDEX = 0;
    public static final String BY_PERIOD_REGEX = "\\.";
    public static final String POST_FIX = "-validation-results.json";

    public Path responsePathFor(Path filePath) {
        final Path parent = filePath.getParent();
        final String fileNameAbbreviation = getFileNameAbbreviation(filePath);
        return Path.of(parent.toString(), createFileName(fileNameAbbreviation));
    }

    private String createFileName(String fileNameAbbreviation) {
        return fileNameAbbreviation + POST_FIX;
    }

    private String getFileNameAbbreviation(Path filePath) {
        final Path fileName = filePath.getFileName();
        return fileName.toString().split(BY_PERIOD_REGEX)[FILE_NAME_ABBREVIATION_INDEX];
    }
}
