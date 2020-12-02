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

from validation.rules.providers.ActiveRulesProvider import ActiveRulesProvider

from validation.rules.table.ActiveRuleEntity import ActiveRuleEntity
from validation.rules.table.ActiveRulesTable import ActiveRulesTable


class ActiveRulesTableGenerator:

    def __init__(self,
                 rules_descriptions_providers: list,
                 active_rules_provider: ActiveRulesProvider
                 ):
        self._rules_descriptions = rules_descriptions_providers
        self._active_rules = active_rules_provider

    def generate_active_validation_rule_tables(self, reqs_tags: list) -> list:
        tables = []
        for reqs_tag in reqs_tags:
            tables.append(
                self._create_rules_table(reqs_tag)
            )
        return tables

    def _create_rules_table(self, reqs_tag: str) -> ActiveRulesTable:
        active_rules_table = ActiveRulesTable()
        active_rules = self._active_rules.get_active_rules(reqs_tag)
        for rule in active_rules:
            yaml_description = self._get_rule_description(rule)
            active_rules_table.add_entity(
                ActiveRuleEntity(
                    rule,
                    self._get_description_from_yaml(yaml_description),
                    self._get_product_from_yaml(yaml_description)
                )
            )
        return active_rules_table

    def _get_rule_description(self, rule: str) -> dict:
        yaml_description = ""
        for rules_description in self._rules_descriptions:
            if rules_description.contains_rule(rule):
                yaml_description = rules_description.get_rule_description(rule)
                break
        return yaml_description

    @staticmethod
    def _get_description_from_yaml(yaml_file: dict) -> str:
        return yaml_file['description'].replace("\n", " ")

    @staticmethod
    def _get_product_from_yaml(yaml_file: dict) -> str:
        return yaml_file['info']['product']
