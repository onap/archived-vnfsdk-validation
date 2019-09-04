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

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	public static final Logger logger = LoggerFactory.getLogger(ValidationException.class);
	private String errorMessage;
    private ErrorCodes errorCode;
    private final transient Map<String,Object> properties = new TreeMap<>();

    public  ValidationException(){
        super();
    }

    public static ValidationException wrappedInfo(Throwable exception) {
        return wrappedInfo(exception, null);
    }

    public ValidationException(ErrorCodes errCode, String message) {
        super(message);
    }

    public ValidationException(ErrorCodes fileIo) {
        this.errorCode = fileIo;
    }

    public ValidationException(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ValidationException(Throwable cause, ErrorCodes errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public ValidationException(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String toString(){
        return ("Exception Number =  "+errorMessage) ;
    }

    public static ValidationException wrappedInfo(Throwable exception, ErrorCodes errorCode) {
        if (exception instanceof ValidationException) {
            ValidationException se = (ValidationException)exception;
        	if (errorCode != null) {
                return new ValidationException(exception.getMessage(), exception, errorCode);
			}
			return se;
        } else {
            return new ValidationException(exception.getMessage(), exception, errorCode);
        }
    }
    

    

	
	public ErrorCodes getErrorCode() {
        return errorCode;
    }
	
	public ValidationException setErrorCode(ErrorCodes errorCode) {
        this.errorCode = errorCode;
        return this;
    }
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
    @SuppressWarnings("unchecked")
	public <T> T get(String name) {
        return (T)properties.get(name);
    }
	
    public ValidationException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }
}
