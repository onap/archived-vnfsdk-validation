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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public final class FileUtil {

    public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final int TRY_COUNT = 3;

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
            logger.error("FILE_IO" + ":" + "close InputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
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
        	logger.error("FILE_IO" + ":" + "close OutputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
    }
    
    public static void closeFileStream(FileInputStream ifs) {
        try {
            if (ifs != null) {
                ifs.close();
            }
        } catch (Exception e1) {
        	logger.error("FILE_IO" + ":" + "close OutputStream error! "+ErrorCodes.FILE_IO+ " " + e1.getMessage(), e1);
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

    public static boolean writeJsonDatatoFile(String fileAbsPath, Object obj) 
    {   
        logger.info("Write JsonData to file :"+fileAbsPath);
        
        boolean bResult = false;
        if(checkFileExists(fileAbsPath))
        {
            deleteFile(fileAbsPath);
        }
        
        ObjectMapper mapper = new ObjectMapper();       
        try 
        {
            mapper.writeValue(new File(fileAbsPath), obj);
            bResult = true;
        } 
        catch (JsonGenerationException e) 
        {
            logger.error("JSON_GENERATION" + ":" + "JsonGenerationException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : " +ErrorCodes.JSON_GENERATION_ERROR+" " + e.getMessage(), e);
            throw new ValidationException(ErrorCodes.JSON_GENERATION_ERROR);
        } 
        catch (JsonMappingException e) 
        {
            logger.error("JSON_MAPPING" + ":" + "JsonMappingException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : " +ErrorCodes.JSON_MAPPING_FAILED+" " + e.getMessage(), e);
            throw new ValidationException(ErrorCodes.JSON_MAPPING_FAILED);
        } 
        catch (IOException e) 
        {
            logger.error("FILE_IO" + ":" + "IOException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : " +ErrorCodes.FILE_IO+" " + e.getMessage(), e);
            throw new ValidationException(ErrorCodes.FILE_IO);
        } 
        return bResult;
    }

    public static <T> Object readJsonDatafFromFile(String fileAbsPath, Class<T> clazz)
    {
        if(!checkFileExists(fileAbsPath))
        {
            logger.info("read JsonData from file , file not found :"+fileAbsPath);
            return null;
        }
        
        logger.info("read JsonData from file :"+fileAbsPath);
        
        T obj = null;        
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try 
        {
            obj = mapper.readValue(new File(fileAbsPath), clazz);
        } 
        catch (JsonParseException e1) 
        {
            logger.error("JSON_PARSING" + ":" + "JsonParseException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : "  + e1.getMessage(), e1);
            logger.error("CSAR extraction error ! " +ErrorCodes.PARSE_ERROR);
            throw new ValidationException(ErrorCodes.PARSE_ERROR);
        } 
        catch (JsonMappingException e1) 
        {
        	logger.error("JSON_MAPPING" + ":" + "JsonMappingException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : "  + e1.getMessage(), e1);
            logger.error("CSAR extraction error ! " +ErrorCodes.JSON_MAPPING_FAILED);
            throw new ValidationException(ErrorCodes.JSON_MAPPING_FAILED);
        } 
        catch (IOException e1) 
        {
        	logger.error("FILE_IO" + ":" + "IOException Exception: writeJsonDatatoFile-->"+fileAbsPath+" : "  + e1.getMessage(), e1);
            logger.error("CSAR extraction error ! " +ErrorCodes.FILE_IO);
            throw new ValidationException(ErrorCodes.FILE_IO);
        }
        return obj;
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
