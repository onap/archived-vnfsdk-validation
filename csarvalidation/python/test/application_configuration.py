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
import pathlib
from jproperties import Properties


def get_releases(path_to_properties: str) -> str:
    configs = Properties()
    with open(path_to_properties, 'rb') as properties:
        configs.load(properties)

    return configs.get('releases.order').data


def get_path_to_csarvalidate_folder(current_location: str) -> str:
    return str(pathlib.Path(current_location).parent.parent.parent.absolute())


RESOURCES_DIRECTORY = get_path_to_csarvalidate_folder(__file__) + '/src/main/resources/'
RULE_DESCRIPTION_SOL001_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol001/'
RULE_DESCRIPTION_SOL004_PATH = RESOURCES_DIRECTORY + 'open-cli-schema/sol004/'
VNFREWS_PROPERTIES_PATH = RESOURCES_DIRECTORY + 'vnfreqs.properties'
RULE_DESCRIPTION_FILE_NAME_PATTERN = 'vtp-validate-csar-%s.yaml'

CSV_DELIMITER = ";"

VNF_REQS_TAG = "vnfreqs"
PNF_REQS_TAG = "pnfreqs"
