/**
 * Copyright 2020 Nokia
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
 */

package org.onap.functional.cli;


import java.util.List;
import java.util.Objects;

public class OnapCliValidationResponseWrapper {

    public final VnfDataWrapper vnf;
    public final String data;
    public final String criteria;
    public final List<ValidationResultWrapper> results;
    public final String contact;
    public final String platform;

    public OnapCliValidationResponseWrapper(VnfDataWrapper vnf, String data, String criteria, List<ValidationResultWrapper> results, String contact, String platform) {
        this.vnf = vnf;
        this.data = data;
        this.criteria = criteria;
        this.results = results;
        this.contact = contact;
        this.platform = platform;
    }

    public static class VnfDataWrapper {
        public String name;
        public String vendor;
        public String version;
        public String type;
        public String mode;
    }

    public static class ValidationResultWrapper {
        public Boolean passed;
        public String vnfreqName;
        public String description;
        public List<ValidationErrorWrapper> errors;
        public List<ValidationErrorWrapper> warnings;

        public static class ValidationErrorWrapper {
            public String vnfreqNo;
            public String code;
            public String message;
            public String file;
            public String lineNumber;

            public ValidationErrorWrapper(String vnfreqNo, String code, String message, String file, String lineNumber) {
                this.vnfreqNo = vnfreqNo;
                this.code = code;
                this.message = message;
                this.file = file;
                this.lineNumber = lineNumber;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return o.hashCode() == this.hashCode();
            }

            @Override
            public int hashCode() {
                return Objects.hash(vnfreqNo, code, message, file, lineNumber);
            }
        }
    }
}
