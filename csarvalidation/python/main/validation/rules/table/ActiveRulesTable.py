# ============LICENSE_START====================================
# vnfsdk-validation
# =========================================================
# Copyright (C) 2021 Nokia. All rights reserved.
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

from validation.rules.table.ActiveRuleEntity import ActiveRuleEntity


class ActiveRulesTable:

    def __init__(self):
        self._active_rules_entities = []

    def add_entity(self, active_rule_entity: ActiveRuleEntity):
        self._active_rules_entities.append(active_rule_entity)

    def get_table_in_csv_format(self, values_separator=";", entity_separator="\n") -> str:
        csv_table = ""
        active_rules_entries_sorted_by_release = ActiveRulesTable._sort_by_release(self._active_rules_entities)
        for entity in active_rules_entries_sorted_by_release:
            csv_table += \
                entity.product + values_separator + \
                entity.release + values_separator + \
                entity.rule + values_separator + \
                entity.description + entity_separator
        return csv_table

    @staticmethod
    def _sort_by_release(active_rules_entities: list) -> list:
        return sorted(active_rules_entities, key=lambda it: it.release)
