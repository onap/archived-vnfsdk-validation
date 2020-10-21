import os.path
import yaml

RESOURCES_DIRECTORY = '../resources/'
OUTPUT_DIRECTORY = RESOURCES_DIRECTORY + 'generated/active-validation-rules/'

VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
PULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

TABLE_WITH_VNF_RULES = OUTPUT_DIRECTORY + 'VnfActiveRulesTable.csv'
TABLE_WITH_PNF_RULES = OUTPUT_DIRECTORY + 'PnfActiveRulesTable.csv'

CSV_DELIMITER = ";"


def load_file_by_lines():
    with open(VNFREWS_PROPERTIES_PATH, 'r') as properties_file:
        lines = properties_file.read().splitlines()
    return lines


def create_output_directory():
    if not os.path.isdir(OUTPUT_DIRECTORY):
        os.makedirs(OUTPUT_DIRECTORY)


def get_rules_from_line(line):
    return line.split("=")[1].split(",")


def ged_description_from_yaml(yaml_file):
    with open(yaml_file, 'r') as description_file:
        description_yaml = yaml.load(description_file, Loader=yaml.FullLoader)
        return description_yaml['description'].replace("\n", " ")


def get_rule_description(rule):
    description_file_name = PULE_DESCRIPTION_FILE_NAME_PATTERN % rule
    description = ""
    if os.path.isfile(RULE_DESCRIPTION_SOL001_PATH + description_file_name):
        description = ged_description_from_yaml(RULE_DESCRIPTION_SOL001_PATH + description_file_name)
    elif os.path.isfile(RULE_DESCRIPTION_SOL004_PATH + description_file_name):
        description = ged_description_from_yaml(RULE_DESCRIPTION_SOL004_PATH + description_file_name)
    return description


def write_rules_to_file(file_name, line):
    with open(file_name, 'w') as vnf_rules_file:
        rules = get_rules_from_line(line)
        for rule in rules:
            vnf_rules_file.write(rule + CSV_DELIMITER)
            vnf_rules_file.write(get_rule_description(rule))
            vnf_rules_file.write("\n")


vnfrews_lines = load_file_by_lines()
create_output_directory()
write_rules_to_file(TABLE_WITH_VNF_RULES, vnfrews_lines[0])
write_rules_to_file(TABLE_WITH_PNF_RULES, vnfrews_lines[1])
