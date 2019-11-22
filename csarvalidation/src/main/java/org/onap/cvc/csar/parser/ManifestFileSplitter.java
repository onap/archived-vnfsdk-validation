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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileFormat {
    public enum FileType { WINDOWS, UNIX, MAC, UNKNOWN }

    private static final char CR = '\r';
    private static final char LF = '\n';

    private FileFormat() {
    }

    public static FileType discover(String fileName) throws IOException {

        try (Reader reader = new BufferedReader(new FileReader(fileName))) {
            return discover(reader);
        }
    }

    public static String getNewLine(String fileName) throws IOException {
        switch(discover(fileName)) {
            case WINDOWS: return String.valueOf(CR) + String.valueOf(LF);
            case MAC: return String.valueOf(CR);
            default: return String.valueOf(LF);
        }
    }

    private static FileType discover(Reader reader) throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            switch(c) {
                case LF: return FileType.UNIX;
                case CR: {
                    if (reader.read() == LF)
                        return FileType.WINDOWS;
                    else
                        return FileType.MAC;
                }
                default: continue;
            }
        }
        return FileType.UNKNOWN;
    }
}

public class ManifestFileSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(ManifestFileSplitter.class);

    public ManifestFileModel split(File manifestFile) {
        String fileName = manifestFile.getAbsolutePath();
        List<String> data = new ArrayList<>();
        List<String> cms = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            List<String> lines = stream.collect(Collectors.toList());
            return createManifestFileModel(data, cms, lines, FileFormat.getNewLine(fileName));

        } catch (IOException e) {
            LOG.error("Unable to process manifest file!", e);
            throw new IllegalArgumentException(String.format("Unable to process manifest file! Wrong file path: '%s'", fileName));
        }
    }

    private ManifestFileModel createManifestFileModel(List<String> data, List<String> cms, List<String> lines, String newLine) {
        boolean isCmsSection = false;

        for (String line : lines) {
            if (line.contains("BEGIN CMS")) {
                isCmsSection = true;
            }

            if (isCmsSection) {
                cms.add(line);
            } else {
                data.add(line);
            }
        }
        return new ManifestFileModel(data, cms, newLine);
    }
}
