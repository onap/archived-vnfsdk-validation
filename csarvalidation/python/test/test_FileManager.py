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

import os
import shutil
import unittest
import application_configuration
import assertion

from validation.FileManager import FileManager
from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator
from validation.rules.providers.ActiveRulesProvider import ActiveRulesProvider
from validation.rules.providers.RulesDescriptionsProvider import RulesDescriptionsProvider

RESOURCES_DIRECTORY = application_configuration.get_path_to_csarvalidate_folder(__file__)+'/src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

CSV_DELIMITER = ";"

VNF_REQS_TAG = "vnfreqs"
PNF_REQS_TAG = "pnfreqs"

OUTPUT_DIRECTORY = './active_rules_table_generator_test/'

TABLE_WITH_VNF_RULES = OUTPUT_DIRECTORY + 'VnfActiveRulesTable.csv'
TABLE_WITH_PNF_RULES = OUTPUT_DIRECTORY + 'PnfActiveRulesTable.csv'


class FileManagerTest(unittest.TestCase):


    @staticmethod
    def generate_tables_with_active_rules(requs_tags: list) -> list:
        return ActiveRulesTableGenerator(
            [
                RulesDescriptionsProvider(RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN),
                RulesDescriptionsProvider(RULE_DESCRIPTION_SOL004_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN)
            ],
            ActiveRulesProvider(VNFREWS_PROPERTIES_PATH)
        ).generate_active_validation_rule_tables(requs_tags)

    def generate_and_save_tables(self):
        tables = self.generate_tables_with_active_rules([VNF_REQS_TAG, PNF_REQS_TAG])
        file_manager = FileManager(OUTPUT_DIRECTORY)
        file_manager.save_rule_table(
            tables[0].get_table_in_csv_format(values_separator=CSV_DELIMITER),
            TABLE_WITH_VNF_RULES
        )
        file_manager.save_rule_table(
            tables[1].get_table_in_csv_format(values_separator=CSV_DELIMITER),
            TABLE_WITH_PNF_RULES
        )

    def tearDown(self):
        shutil.rmtree(OUTPUT_DIRECTORY)

    def test_generate_and_save_table_with_active_rules(self):
        self.generate_and_save_tables()
        self.assertTrue(os.path.isdir(OUTPUT_DIRECTORY))

        self.assertTrue(os.path.isfile(TABLE_WITH_VNF_RULES))
        with open(TABLE_WITH_VNF_RULES, 'r') as vnf_rules:
            self.validate_csv_table_with_rules(vnf_rules)

        self.assertTrue(os.path.isfile(TABLE_WITH_PNF_RULES))
        with open(TABLE_WITH_PNF_RULES, 'r') as pnf_rules:
            self.validate_csv_table_with_rules(pnf_rules)

    def validate_csv_table_with_rules(self, vnf_rules):
        releases = application_configuration.get_releases(VNFREWS_PROPERTIES_PATH)
        lines = vnf_rules.read().splitlines()
        for line in lines:
            assertion.verify_that_cvc_line_is_valid(self, line, releases, CSV_DELIMITER)


if __name__ == '__main__':
    unittest.main()
