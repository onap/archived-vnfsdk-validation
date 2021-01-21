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

import unittest

import application_configuration
import assertion
from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator
from validation.rules.providers.ActiveRulesProvider import ActiveRulesProvider
from validation.rules.providers.RulesDescriptionsProvider import RulesDescriptionsProvider
from validation.rules.table.ActiveRuleEntity import ActiveRuleEntity
from validation.rules.table.ActiveRulesTable import ActiveRulesTable


class ActiveRulesTableGeneratorTest(unittest.TestCase):

    @staticmethod
    def generate_tables_with_active_rules(requs_tags: list) -> list:
        return ActiveRulesTableGenerator(
            [
                RulesDescriptionsProvider(application_configuration.RULE_DESCRIPTION_SOL001_PATH,
                                          application_configuration.RULE_DESCRIPTION_FILE_NAME_PATTERN),
                RulesDescriptionsProvider(application_configuration.RULE_DESCRIPTION_SOL004_PATH,
                                          application_configuration.RULE_DESCRIPTION_FILE_NAME_PATTERN)
            ],
            ActiveRulesProvider(application_configuration.VNFREWS_PROPERTIES_PATH)
        ).generate_active_validation_rule_tables(requs_tags)

    def test_generate_table_with_active_pnf_rules(self):
        tables = self.generate_tables_with_active_rules([application_configuration.PNF_REQS_TAG])

        self.assertTrue(len(tables) == 1)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format())

    def test_generate_table_with_active_vnf_rules(self):
        tables = self.generate_tables_with_active_rules([application_configuration.VNF_REQS_TAG])

        self.assertTrue(len(tables) == 1)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format())

    def test_generate_tables_with_active_vnf_and_pnf_rules(self):
        tables = self.generate_tables_with_active_rules([application_configuration.PNF_REQS_TAG,
                                                         application_configuration.VNF_REQS_TAG])

        self.assertTrue(len(tables) == 2)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format(values_separator=
                                                                             application_configuration.CSV_DELIMITER))
        self.validate_csv_table_with_rules(tables[1].get_table_in_csv_format(values_separator=
                                                                             application_configuration.CSV_DELIMITER))

    def validate_csv_table_with_rules(self, vnf_rules: str):
        releases = application_configuration.get_releases(application_configuration.VNFREWS_PROPERTIES_PATH)
        lines = vnf_rules.splitlines()
        for line in lines:
            assertion.verify_that_cvc_line_is_valid(self, line, releases, application_configuration.CSV_DELIMITER)

    def test_sort_active_rule_entries_by_release(self):
        # given
        rule_entries = [
            ActiveRuleEntity(product="onap-vtp", rule="r-1", release="honolulu", description="Some desc"),
            ActiveRuleEntity(product="onap-vtp", rule="r-2", release="guilin", description="Some desc"),
            ActiveRuleEntity(product="onap-vtp", rule="r-3", release="amsterdam", description="Some desc"),
            ActiveRuleEntity(product="onap-vtp", rule="r-4", release="casablanca", description="Some desc"),
            ActiveRuleEntity(product="onap-vtp", rule="r-5", release="frankfurt", description="Some desc"),
            ActiveRuleEntity(product="onap-vtp", rule="r-6", release="amsterdam", description="Some desc")
        ]

        # when
        sorted_entries = ActiveRulesTable._sort_by_release(rule_entries)

        # then
        self.assertTrue(sorted_entries[0].release == 'amsterdam')
        self.assertTrue(sorted_entries[1].release == 'amsterdam')
        self.assertTrue(sorted_entries[2].release == 'casablanca')
        self.assertTrue(sorted_entries[3].release == 'frankfurt')
        self.assertTrue(sorted_entries[4].release == 'guilin')
        self.assertTrue(sorted_entries[5].release == 'honolulu')




if __name__ == '__main__':
    unittest.main()
