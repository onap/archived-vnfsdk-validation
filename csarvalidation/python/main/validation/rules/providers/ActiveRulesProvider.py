# ============LICENSE_START====================================
# vnfsdk-validation
# =========================================================
# Copyright (C) 2020 Nokia. All rights reserved.
# =========================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=====================================

class ActiveRulesProvider:

    def __init__(self, properties_file_path: str, rules_separator: str = ",",  key_value_separator: str = "="):
        self.properties_file_path = properties_file_path
        self._rules_separator = rules_separator
        self._key_value_separator = key_value_separator

    def get_active_rules(self, reqs: str) -> list:
        active_rules = []
        for line in self._load_file_by_lines():
            if line.startswith(reqs):
                active_rules = self._split_properties_line(line, self._rules_separator, self._key_value_separator)
                break
        return active_rules

    def _load_file_by_lines(self) -> list:
        with open(self.properties_file_path, 'r') as properties_file:
            lines = properties_file.read().splitlines()
        return lines

    @staticmethod
    def _split_properties_line(line: str, splitter: str, key_value_separator: str) -> list:
        key_value_separation_index = line.index(key_value_separator) + 1
        value = line[key_value_separation_index:]
        return value.split(splitter)
