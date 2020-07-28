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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;


public final class FileUtil {

    public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final int TRY_COUNT = 3;
    public static final String FILE_IO_STR = "FILE_IO";

    private FileUtil() {

    }


    /**
     * create dir.
     * @param dir dir to create
     * @return boolean
     */
    public static boolean createDirectory(String dir) {
        File folder = new File(dir);
        int tryCount = 0;
        while (tryCount < TRY_COUNT) {
            tryCount++;
            if (!folder.exists() && !folder.mkdirs()) {
                continue;
            } else {
                return true;
            }
        }

        return folder.exists();
    }

    /**
     * delete file.
     * @param file the file to delete
     * @return boolean
     */
    public static boolean deleteFile(File file) {
        String hintInfo = file.isDirectory() ? "dir " : "file ";
        boolean isFileDeleted = file.delete();
        boolean isFileExist = file.exists();
        if (!isFileExist) {
            if (isFileDeleted) {
                logger.info("delete " + hintInfo + file.getAbsolutePath());
            } else {
                isFileDeleted = true;
                logger.info("file not exist. no need delete " + hintInfo + file.getAbsolutePath());
            }
        } else {
            logger.info("fail to delete " + hintInfo + file.getAbsolutePath());

        }
        return isFileDeleted;
    }


    /**
     * close InputStream.
     * 
     * @param inputStream the inputstream to close
     * @throws ValidationException 
     */
    public static void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e1) {
            logger.error(FILE_IO_STR + ":" + "close InputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }

    /**
     * close OutputStream.
     * 
     * @param outputStream the output stream to close
     * @throws ValidationException 
     */
    public static void closeOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e1) {
            logger.error(FILE_IO_STR + ":" + "close OutputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }
    
    public static void closeFileStream(FileInputStream ifs) {
        try {
            if (ifs != null) {
                ifs.close();
            }
        } catch (Exception e1) {
            logger.error(FILE_IO_STR + ":" + "close OutputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }

    /**
     * close zipFile.
     * 
     * @param zipFile the zipFile to close
     * @throws ValidationException 
     */
    public static void closeZipFile(ZipFile zipFile) {
        try {
            if (zipFile != null) {
                zipFile.close();
                }
        } catch (IOException e1) {
		    logger.error("CLOSE_ZIPFILE" + ":" + "close ZipFile error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
			throw new ValidationException(ErrorCodes.FILE_IO); 
        }
    }

    public static boolean checkFileExists(String filePath)
    {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean deleteFile(String filePath)
    {
        File file = new File(filePath);
        return deleteFile(file);
    }
    public static boolean  deleteDirectory(String path) 
    {
        File file = new File(path);
        return deleteDirectory(file);
    }
    
    public static boolean  deleteDirectory(File file) 
    {
        if (!file.exists())
        {
            return true;          
        }        
        if (file.isDirectory()) 
        {
            for (File f : file.listFiles())
            {
                deleteDirectory(f);
            }
        }
        return file.delete();
    }
}
