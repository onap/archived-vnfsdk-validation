/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.validation.csar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Map;

public class CsarUtil {

    private static final Logger logger = LoggerFactory.getLogger(CsarUtil.class);

    public static String getUnzipDir(String dirName) {
        File tmpDir = new File(File.separator + dirName);
        return tmpDir.getAbsolutePath().replace(".csar", "");
    }

    /**
     * unzip zip file.
     *
     * @param zipFileName
     *            file name to zip
     * @param extPlace
     *            extPlace
     * @return unzip file names in zip
     * @throws IOException
     *             e1
     * @throws ValidationException
     */
    public static Map<String, String> unzip(String zipFileName, String extPlace) throws IOException {
        HashMap<String, String> unzipFileNames = new HashMap<>();

        try(ZipFile zipFile = new ZipFile(zipFileName)) {

            Enumeration<?> fileEn = zipFile.entries();
            byte[] buffer = new byte[CommonConstants.BUFFER_SIZE];

            while (fileEn.hasMoreElements()) {
                InputStream input = null;
                BufferedOutputStream bos = null;
                ZipEntry entry = (ZipEntry) fileEn.nextElement();
                if (!entry.isDirectory()) {
                try {
                    input = zipFile.getInputStream(entry);
                    File file = new File(extPlace, entry.getName());

                     //Currently it does not support xml based VNF descriptors.
					 //So skip and proceed to YAML defined files validation only.
                    if (file.getAbsolutePath().contains("xml"+System.getProperty("file.separator"))) {
                        continue;
                    }

                    if (!file.getParentFile().exists()) {
                        FileUtil.createDirectory(file.getParentFile().getAbsolutePath());
                    }

                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    while (true) {
                        int length = input.read(buffer);
                        if (length == -1) {
                            break;
                        }
                        bos.write(buffer, 0, length);
                    }

                    unzipFileNames.put(file.getName(), file.getAbsolutePath());

                } finally {
                    closeOutputStream(bos);
                    closeInputStream(input);
                }
                }
            }
        }
        return unzipFileNames;
    }

    /**
     * close InputStream.
     *
     * @param inputStream
     *            the inputstream to close
     * @throws ValidationException
     */
    public static void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e1) {
            logger.error("FILE_IO" + ":" + "close InputStream error! " +ErrorCodes.FILE_IO+" "+ e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }

    /**
     * close OutputStream.
     *
     * @param outputStream
     *            the output stream to close
     * @throws ValidationException
     */
    public static void closeOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e1) {
            logger.error("FILE_IO" + ":" + "close OutputStream error! " +ErrorCodes.FILE_IO, e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
            
        }
    }

    /**
     *
     * @param filePath
     * @return HashMap<String, String>
     */
    public static Map<String, String> csarExtract(String filePath) {

        try {
            String tempfolder = CsarUtil.getUnzipDir(filePath);
            return CsarUtil.unzip(filePath, tempfolder);

        } catch (IOException e1) {
            logger.error("CSAR_EXTRACTION" + ":" + "CSAR extraction error ! " +ErrorCodes.FILE_IO+" "+ e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }

}
