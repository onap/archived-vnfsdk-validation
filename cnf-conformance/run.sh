#!/bin/sh

#    Copyright (C) 2020, Huawei Technologies, Ltd.
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#            http://www.apache.org/licenses/LICENSE-2.0
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

#Helps to run cnf-conformance test cases.
#Follow the guidelines detailed in https://github.com/cncf/cnf-conformance

# Arg-1: CNF dir where the cnf-conformance.yml is placed
	
cd $1

cnf-conformance setup > setup.txt
cnf-conformance all cnf-config=./cnf-conformance.yml > all.txt

cat setup.txt
cat all.txt
