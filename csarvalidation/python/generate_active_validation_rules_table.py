from validation.rules.ActiveRulesTableGenerator import ActiveRulesTableGenerator

RESOURCES_DIRECTORY = '../src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

OUTPUT_DIRECTORY = RESOURCES_DIRECTORY + 'generated/active-validation-rules/'

TABLE_WITH_VNF_RULES = OUTPUT_DIRECTORY + 'VnfActiveRulesTable.csv'
TABLE_WITH_PNF_RULES = OUTPUT_DIRECTORY + 'PnfActiveRulesTable.csv'


def main():
    ActiveRulesTableGenerator(
        RESOURCES_DIRECTORY, RULE_DESCRIPTION_SOL001_PATH, RULE_DESCRIPTION_SOL004_PATH,
        VNFREWS_PROPERTIES_PATH, RULE_DESCRIPTION_FILE_NAME_PATTERN,
        TABLE_WITH_VNF_RULES, TABLE_WITH_PNF_RULES,
        OUTPUT_DIRECTORY
    ).generate_active_validation_rule_table()


if __name__ == '__main__':
    main()
