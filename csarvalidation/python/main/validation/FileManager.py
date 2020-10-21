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

import os


class FileManager:

    def __init__(self, output_directory: str):
        self._output_directory = output_directory

    def save_rule_table(self, csv_table: str, table_name: str):
        self._create_output_directory()
        self._write_rules_to_file(csv_table, table_name)

    def _create_output_directory(self):
        if not os.path.isdir(self._output_directory):
            os.makedirs(self._output_directory)

    def _write_rules_to_file(self, csv_table: str, file_name: str):
        with open(file_name, 'w') as vnf_rules_file:
            vnf_rules_file.write(csv_table)
