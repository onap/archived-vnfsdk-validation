/*
  Copyright 2020 Nokia
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.onap.cvc.csar.cc.sol004.r130206.artifact;

import java.nio.file.Path;

public class ArtifactSecurityFileValidatorWithCommonFile extends ArtifactSecurityFileValidator {

    private final Path commonSecurityFilePath;

    ArtifactSecurityFileValidatorWithCommonFile(Path rootDirectory, String artifactRelativeFilePath, String securityRelativeFilePath, Path commonSecurityFilePath) {
        super(rootDirectory, artifactRelativeFilePath, securityRelativeFilePath);
        this.commonSecurityFilePath = commonSecurityFilePath;
    }

    @Override
    public ValidatedSecurityFile getValidatedSecurityFile(String pathToSourceFile, String pathToSecurityFile, String[] securityFileExtensions, String securityFileType) {
        ValidatedSecurityFile  baseValidationResult =
            super.getValidatedSecurityFile(pathToSourceFile, pathToSecurityFile, securityFileExtensions, securityFileType);
        if(baseValidationResult.getFilePath() == null) {
            return new ValidatedSecurityFile(commonSecurityFilePath,baseValidationResult.getErrors());
        } else {
            return baseValidationResult;
        }
    }
}
