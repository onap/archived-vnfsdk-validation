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


def verify_that_cvc_line_is_valid(testcase: unittest.TestCase, line: str, releases: str, delimiter: str):
    values = line.split(delimiter)
    testcase.assertTrue(len(values) == 4)
    testcase.assertTrue(values[0].startswith("onap-"))
    testcase.assertTrue(
        values[1] in releases,
        msg="Rule '{}' has wrong release name '{}'. Release name must match to one of '{}'".format(
            values[2], values[1], releases
        )
    )
    testcase.assertTrue(values[2].startswith("r"))
