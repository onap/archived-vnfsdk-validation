from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator
import os

RESOURCES_DIRECTORY = '../src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

DEFAULT_OUTPUT_DIRECTORY = RESOURCES_DIRECTORY + 'generated/active-validation-rules/'


def main():
    output_directory = get_output_directory()
    table_with_vnf_rules = output_directory + 'VnfActiveRulesTable.csv'
    table_with_pnf_rules = output_directory + 'PnfActiveRulesTable.csv'

    ActiveRulesTableGenerator(
        RESOURCES_DIRECTORY, RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_SOL004_PATH,
        VNFREWS_PROPERTIES_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN,
        table_with_vnf_rules, table_with_pnf_rules,
        output_directory
    ).generate_active_validation_rule_table()


def get_output_directory():
    if 'OUTPUT_DIRECTORY' in os.environ:
        output_directory = os.getenv('OUTPUT_DIRECTORY')
    else:
        output_directory = DEFAULT_OUTPUT_DIRECTORY
    return output_directory


if __name__ == '__main__':
    main()
