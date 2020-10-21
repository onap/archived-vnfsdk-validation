import os

import yaml


class ActiveRulesTableGenerator:

    def __init__(self,
                 resource_directory, sol001_directory, sol004_directory,
                 validation_properties_file_name, rule_description_file_name_pattern,
                 table_with_vnf_rules_file_name, table_with_pnf_rules_file_name,
                 output_directory, csv_delimiter=";"
                 ):
        self._resource_directory = resource_directory
        self._sol001_directory = sol001_directory
        self._sol004_directory = sol004_directory
        self._validation_properties_file_name = validation_properties_file_name
        self._rule_description_file_name_pattern = rule_description_file_name_pattern
        self._table_with_vnf_rules_file_name = table_with_vnf_rules_file_name
        self._table_with_pnf_rules_file_name = table_with_pnf_rules_file_name
        self._output_directory = output_directory
        self._csv_delimiter = csv_delimiter

    def _load_validation_properties_by_lines(self):
        with open(self._validation_properties_file_name, 'r') as properties_file:
            lines = properties_file.read().splitlines()
        return lines

    def _create_output_directory(self):
        if not os.path.isdir(self._output_directory):
            os.makedirs(self._output_directory)

    def _get_rules_from_line(self, line):
        key_value_separation_index = line.index("=") + 1
        value = line[key_value_separation_index:]
        return value.split(",")

    def _ged_yaml_description(self, yaml_file):
        with open(yaml_file, 'r') as description_file:
            description_yaml = yaml.load(description_file, Loader=yaml.FullLoader)
            return description_yaml

    def _ged_description_from_yaml(self, yaml_file):
        return yaml_file['description'].replace("\n", " ")

    def _ged_product_from_yaml(self, yaml_file):
        return yaml_file['info']['product']

    def _get_rule_description(self, rule):
        description_file_name = self._rule_description_file_name_pattern % rule
        yaml_description = ""
        if os.path.isfile(self._sol001_directory + description_file_name):
            yaml_description = self._ged_yaml_description(self._sol001_directory + description_file_name)
        elif os.path.isfile(self._sol004_directory + description_file_name):
            yaml_description = self._ged_yaml_description(self._sol004_directory + description_file_name)
        return yaml_description

    def _write_rules_to_file(self, file_name, line):
        with open(file_name, 'w') as vnf_rules_file:
            rules = self._get_rules_from_line(line)
            for rule in rules:
                yaml_description = self._get_rule_description(rule)
                vnf_rules_file.write(self._ged_product_from_yaml(yaml_description) + self._csv_delimiter)
                vnf_rules_file.write(rule + self._csv_delimiter)
                vnf_rules_file.write(self._ged_description_from_yaml(yaml_description))
                vnf_rules_file.write("\n")

    def generate_active_validation_rule_table(self):
        properties_lines = self._load_validation_properties_by_lines()
        self._create_output_directory()
        self._write_rules_to_file(self._table_with_vnf_rules_file_name, properties_lines[0])
        self._write_rules_to_file(self._table_with_pnf_rules_file_name, properties_lines[1])
