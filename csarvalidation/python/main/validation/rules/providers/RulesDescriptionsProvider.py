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
import yaml


class RulesDescriptionsProvider:

    def __init__(self, directory_containing_rules: str, rule_description_file_name_pattern: str):
        self._directory_containing_rules = directory_containing_rules
        self._rule_description_file_name_pattern = rule_description_file_name_pattern

    def contains_rule(self, rule: str) -> bool:
        description_file_name = self._rule_description_file_name_pattern % rule
        return os.path.isfile(self._directory_containing_rules + description_file_name)

    def get_rule_description(self, rule: str) -> str:
        description_file_name = self._rule_description_file_name_pattern % rule
        return self._ged_yaml_description(description_file_name)

    def _ged_yaml_description(self, yaml_file: str) -> str:
        with open(self._directory_containing_rules + yaml_file, 'r') as description_file:
            description_yaml = yaml.load(description_file, Loader=yaml.FullLoader)
            return description_yaml
