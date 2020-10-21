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

import unittest

from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator
from validation.rules.providers.ActiveRulesProvider import ActiveRulesProvider
from validation.rules.providers.RulesDescriptionsProvider import RulesDescriptionsProvider

RESOURCES_DIRECTORY = '../src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

CSV_DELIMITER = ";"

VNF_REQS_TAG = "vnfreqs"
PNF_REQS_TAG = "pnfreqs"


class ActiveRulesTableGeneratorTest(unittest.TestCase):

    @staticmethod
    def generate_tables_with_active_rules(requs_tags: list) -> list:
        return ActiveRulesTableGenerator(
            [
                RulesDescriptionsProvider(RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN),
                RulesDescriptionsProvider(RULE_DESCRIPTION_SOL004_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN)
            ],
            ActiveRulesProvider(VNFREWS_PROPERTIES_PATH)
        ).generate_active_validation_rule_tables(requs_tags)

    def test_generate_table_with_active_pnf_rules(self):
        tables = self.generate_tables_with_active_rules([PNF_REQS_TAG])

        self.assertTrue(len(tables) == 1)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format())

    def test_generate_table_with_active_vnf_rules(self):
        tables = self.generate_tables_with_active_rules([VNF_REQS_TAG])

        self.assertTrue(len(tables) == 1)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format())

    def test_generate_tables_with_active_vnf_and_pnf_rules(self):
        tables = self.generate_tables_with_active_rules([PNF_REQS_TAG, VNF_REQS_TAG])

        self.assertTrue(len(tables) == 2)
        self.validate_csv_table_with_rules(tables[0].get_table_in_csv_format(values_separator=CSV_DELIMITER))
        self.validate_csv_table_with_rules(tables[1].get_table_in_csv_format(values_separator=CSV_DELIMITER))

    def validate_csv_table_with_rules(self, vnf_rules: str):
        lines = vnf_rules.splitlines()
        for line in lines:
            values = line.split(CSV_DELIMITER)
            self.assertTrue(len(values) == 3)
            self.assertTrue(values[0].startswith("onap-"))
            self.assertTrue(values[1].startswith("r"))


if __name__ == '__main__':
    unittest.main()
