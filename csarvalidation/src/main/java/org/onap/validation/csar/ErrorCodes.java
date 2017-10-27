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

public enum ErrorCodes {
	
		VALUE_REQUIRED(201),
		INVALID_FORMAT(202),
		VALUE_TOO_SHORT(203),
		VALUE_TOO_LONGS(204),
		VALIDATION_FAILED(205),
		INVALID_CSAR(206),
		INVALID_CSAR_CONTENT(207),
		INVALID_CSAR_TOSCA_CONTENT(208),
		INVALID_CSAR_META_CONTENT(209),
		FILE_IO(210),
		DIR_IO(211),
		ZIP_IO(212), 
		RESOURCE_MISSING(213), 
		PARSE_ERROR(214),
		JSON_MAPPING_FAILED(215), 
		JSON_GENERATION_ERROR(216),
	    SCHEMA_LOAD_ERROR(217);

		private final int number;

		private ErrorCodes(int number) {
			this.number = number;
		}
		

		public int getNumber() {
			return number;
		}

	}

