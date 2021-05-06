/*
 * Copyright 2019 Nokia
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
package org.onap.validation.rule;

public class DictionaryValidateResponse {

    public static final String PLATFORM_VTP_1_0 = "Dictionary Test Platform (VTP) 1.0";
    public static final String ONAP_DISCUSS_LISTS_ONAP_ORG = "ONAP VTP Team onap-discuss@lists.onap.org";
    private final String file;
    private final String date;
    private final String criteria;
    private final String errors;

    public DictionaryValidateResponse(String file, String date, String criteria, String errors) {
        this.file = file;
        this.date = date;
        this.criteria = criteria;
        this.errors = errors;
    }

    public String getFile() {
        return file;
    }

    public String getDate() {
        return date;
    }

    public String getContact() {
        return ONAP_DISCUSS_LISTS_ONAP_ORG;
    }

    public String getPlatform() {
        return PLATFORM_VTP_1_0;
    }

    public String getCriteria() {
        return criteria;
    }

    public String getErrors() {
        return errors;
    }
}
