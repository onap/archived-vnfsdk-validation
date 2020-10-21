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

from validation.FileManager import FileManager
from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator

from validation.rules.providers.ActiveRulesProvider import ActiveRulesProvider
from validation.rules.providers.RulesDescriptionsProvider import RulesDescriptionsProvider

RESOURCES_DIRECTORY = '../../src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

DEFAULT_OUTPUT_DIRECTORY = RESOURCES_DIRECTORY + 'generated/active-validation-rules/'

VNF_REQS_TAG = "vnfreqs"
PNF_REQS_TAG = "pnfreqs"

def main():
    output_directory = get_output_directory()
    table_with_vnf_rules = output_directory + 'VnfActiveRulesTable.csv'
    table_with_pnf_rules = output_directory + 'PnfActiveRulesTable.csv'
    requs_tags = [VNF_REQS_TAG, PNF_REQS_TAG]

    tables = ActiveRulesTableGenerator(
        [
            RulesDescriptionsProvider(RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN),
            RulesDescriptionsProvider(RULE_DESCRIPTION_SOL004_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN)
        ],
        ActiveRulesProvider(VNFREWS_PROPERTIES_PATH)
    ).generate_active_validation_rule_tables(requs_tags)
    file_manager = FileManager(output_directory)
    file_manager.save_rule_table(tables[0].get_table_in_csv_format(), table_with_vnf_rules)
    file_manager.save_rule_table(tables[1].get_table_in_csv_format(), table_with_pnf_rules)


def get_output_directory():
    if 'OUTPUT_DIRECTORY' in os.environ:
        output_directory = os.getenv('OUTPUT_DIRECTORY')
    else:
        output_directory = DEFAULT_OUTPUT_DIRECTORY
    return output_directory


if __name__ == '__main__':
    main()
