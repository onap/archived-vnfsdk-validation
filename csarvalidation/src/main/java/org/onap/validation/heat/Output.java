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

package org.onap.validation.heat;

import java.util.List;
import lombok.Data;

/**
 * Classes for parsing the VVP test result JSON.
 */

@Data
public class Output {
  private String version;
  private String template_directory;
  private String timestamp;
  private String checksum;
  private String categories;
  private String outcome;
  private List<Tests> tests;

  @Data
  static class Tests {
    private String[] files;
    private String test_module;
    private String test_case;
    private String result;
    private String error;
    private List<Requirements> requirements;
  }

  @Data
  static class Requirements {
    private String id;
    private String text;
    private String keyword;
  }

}
