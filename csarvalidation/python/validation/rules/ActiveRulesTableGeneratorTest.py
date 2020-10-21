import os
import shutil
import unittest

from ActiveRulesTableGenerator import ActiveRulesTableGenerator

RESOURCES_DIRECTORY = '../../../src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

OUTPUT_DIRECTORY = './active_rules_table_generator_test/'

TABLE_WITH_VNF_RULES = OUTPUT_DIRECTORY + 'VnfActiveRulesTable.csv'
TABLE_WITH_PNF_RULES = OUTPUT_DIRECTORY + 'PnfActiveRulesTable.csv'

CSV_DELIMITER = ";"


class TestStringMethods(unittest.TestCase):

    def setUp(self):
        ActiveRulesTableGenerator(
            RESOURCES_DIRECTORY, RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_SOL004_PATH,
            VNFREWS_PROPERTIES_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN,
            TABLE_WITH_VNF_RULES, TABLE_WITH_PNF_RULES,
            OUTPUT_DIRECTORY, CSV_DELIMITER
        ).generate_active_validation_rule_table()

    def tearDown(self):
        shutil.rmtree(OUTPUT_DIRECTORY)

    def test_upper(self):
        self.assertTrue(os.path.isdir(OUTPUT_DIRECTORY))

        self.assertTrue(os.path.isfile(TABLE_WITH_VNF_RULES))
        with open(TABLE_WITH_VNF_RULES, 'r') as vnf_rules:
            self.validate_csv_table_with_rules(vnf_rules)

        self.assertTrue(os.path.isfile(TABLE_WITH_PNF_RULES))
        with open(TABLE_WITH_PNF_RULES, 'r') as pnf_rules:
            self.validate_csv_table_with_rules(pnf_rules)

    def validate_csv_table_with_rules(self, vnf_rules):
        lines = vnf_rules.read().splitlines()
        for line in lines:
            values = line.split(CSV_DELIMITER)
            self.assertTrue(len(values) == 3)
            self.assertTrue(values[0].startswith("onap-"))
            self.assertTrue(values[1].startswith("r"))


if __name__ == '__main__':
    unittest.main()
