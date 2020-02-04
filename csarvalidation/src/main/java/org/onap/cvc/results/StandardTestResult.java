/*
 * Copyright © 2019 iconectiv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.cvc.results;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * An attempt at standardizing the output of tests.
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardTestResult {
  private String result;   // pass, fail or skip
  private String testname; // optional name of test
  private String code;     // optional error code
  private String advice;   // optional advice to user
  private String message;  // optional message
  private String error;    // optional error
  private String description; // optional description of test.
}
