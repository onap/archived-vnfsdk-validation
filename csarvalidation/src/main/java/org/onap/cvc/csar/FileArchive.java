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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.onap.cvc.csar.CSARArchive.TEMP_DIR;

public class FileArchive {

    private static final String ZIP_POSTFIX = "zip";
    private static final String CSAR_POSTFIX = ".csar";
    private static final String CERT_POSTFIX = ".cert";
    private static final String CMS_POSTFIX = ".cms";
    private final String tempDir;

    FileArchive(String tempDir){
        this.tempDir = tempDir;
    }

    Workspace unpack(String pathToFile) throws IOException {
        File fileArchive = new File(pathToFile);
        if (!fileArchive.exists()) {
            throw new IllegalArgumentException(String.format("%s does not exist", fileArchive.getName()));
        }

        String path = String.format("%s%s%d", tempDir, File.separator, System.currentTimeMillis());
        Optional<Path> workspaceFolderPath = createWorkspaceFolder(path);
        if(workspaceFolderPath.isPresent()) {
            final Path destination = workspaceFolderPath.get();
            unzip(fileArchive, destination);

            if (pathToFile.endsWith(ZIP_POSTFIX)) {
                return createZipWorkspace(path, destination);
            }else {
                return Workspace.forCsar(destination);
            }
        }

        return Workspace.empty();
    }

    private Workspace createZipWorkspace(String path, Path workspaceFolderPath) throws IOException {

        Optional<Path> pathToCsarFile = findFile(workspaceFolderPath, CSAR_POSTFIX);
        Optional<Path> pathToCertFile = findFile(workspaceFolderPath, CERT_POSTFIX);
        Optional<Path> pathToCmsFile = findFile(workspaceFolderPath, CMS_POSTFIX);

        Path workspaceCsarPath = new File(String.format("%s%scsar", path, File.separator)).toPath();
        if (pathToCsarFile.isPresent()) {
            final Path csarFilePath = pathToCsarFile.get();
            unzip(csarFilePath.toFile(), workspaceCsarPath);

            return Workspace.forZip(
                    workspaceFolderPath,
                    workspaceCsarPath,
                    pathToCertFile.orElse(null),
                    pathToCmsFile.orElse(null),
                    csarFilePath
            );
        }


        return Workspace.forZip(workspaceFolderPath);
    }

    private Optional<Path> findFile(Path workspaceFolderPath, String filePostfix) throws IOException {
        try(Stream<Path> files = Files.find(
                workspaceFolderPath,
                1,
                (p, b)->p.getFileName().toString().endsWith(filePostfix))){
            return files.findFirst();
        }
    }

    private Optional<Path> createWorkspaceFolder(String path) {
        Path destination  = Paths.get(path);
        if(destination.toFile().mkdirs()){
            return Optional.of(destination);
        }

        return Optional.empty();
    }

    private void unzip(File file, Path destination) throws IOException {

        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))){

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
		 
                String pathname = destination + File.separator + entry.getName();
                if (!pathname.startsWith(TEMP_DIR)) {
                    throw new IOException("Entry is outside of the target directory");
                }
                File filePath = new File(destination + File.separator + entry.getName());

                if(entry.isDirectory()){
                    filePath.mkdirs();
                } else {
                    //create directories for sub directories in zip
                    File parentPathFile = filePath.getParentFile();
                    if (parentPathFile != null) {
                        parentPathFile.mkdirs();
                    }
                    extract(zipInputStream, filePath);
                }
            }
        }
    }

    private void extract(ZipInputStream csar, File filePath) throws IOException {
        byte[] buffer = new byte[2048];
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

            int len;
            while ((len = csar.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        }
    }

    public static class Workspace{
        private boolean isZip;
        private Path rootFolder;
        private Path pathToCsarFolder;
        private Path certFile;
        private Path cmsFile;
        private Path csarFile;

        private Workspace(boolean isZip, Path rootFolder,
                Path pathToCsarFolder,
                Path certFile, Path cmsFile,
                Path csarFile) {
            this.isZip = isZip;
            this.rootFolder = rootFolder;
            this.pathToCsarFolder = pathToCsarFolder;
            this.certFile = certFile;
            this.cmsFile = cmsFile;
            this.csarFile = csarFile;
        }

        private Workspace() {
        }

        private Workspace(boolean isZip, Path rootFolder){
            this.isZip = isZip;
            this.rootFolder = rootFolder;
            this.pathToCsarFolder = rootFolder;
        }

        static Workspace empty(){
            return new Workspace();
        }

        static Workspace forCsar(Path workspaceFolder) {
            return new Workspace(false, workspaceFolder);
        }

        static Workspace forZip(Path rootFolder) {
            return new Workspace(true, rootFolder);
        }


        static Workspace forZip(Path rootFolder, Path pathToCsarWorkspace,
                                       Path certFile, Path cmsFile, Path csarFile) {
            return new Workspace(true, rootFolder, pathToCsarWorkspace, certFile, cmsFile, csarFile);
        }

        public boolean isZip() {
            return isZip;
        }

        public Optional<Path> getPathToCsarFolder() {
            return Optional.ofNullable(pathToCsarFolder);
        }

        public Optional<Path> getPathToCertFile() {
            return Optional.ofNullable(certFile);
        }

        public Optional<Path> getRootFolder() {
            return Optional.ofNullable(rootFolder);
        }

        public Optional<Path> getPathToCmsFile() {
            return Optional.ofNullable(cmsFile);
        }

        public Optional<Path> getPathToCsarFile() {
            return Optional.ofNullable(csarFile);
        }
    }

}
