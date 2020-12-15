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

import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.cc.sol004.r130206.Error;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.onap.validation.csar.FileUtil.fileHaveOneOfExtensions;
import static org.onap.validation.csar.FileUtil.filesAreInSameDirectory;
import static org.onap.validation.csar.FileUtil.filesHaveSameNamesIgnoringExtensions;

public class ArtifactSecurityFileValidator {

    private final Path rootDirectory;
    private final String artifactRelativeFilePath;
    private final String securityRelativeFilePath;

    ArtifactSecurityFileValidator(Path rootDirectory, String artifactRelativeFilePath, String securityRelativeFilePath) {
        this.rootDirectory = rootDirectory;
        this.artifactRelativeFilePath = artifactRelativeFilePath;
        this.securityRelativeFilePath = securityRelativeFilePath;
    }

    public ValidatedSecurityFile getValidatedSecurityFile(
        String pathToSourceFile, String pathToSecurityFile,
        String[] securityFileExtensions, String securityFileType
    ) {
        final List<CSARArchive.CSARError> errors = new ArrayList<>();
        Path validatedSecurityFilePath  = null;
        if(!securityRelativeFilePath.isEmpty()) {
            final Path artifactFilePath = rootDirectory.resolve(artifactRelativeFilePath);
            final Path securityFilePath = rootDirectory.resolve(securityRelativeFilePath);
            if (!filesAreInSameDirectory(artifactFilePath, securityFilePath)) {
                errors.add(
                    new Error.CSARErrorWrongSecurityFileLocation(pathToSourceFile, pathToSecurityFile, securityFileType)
                );
            } else if (!filesHaveSameNamesIgnoringExtensions(artifactFilePath, securityFilePath)) {
                errors.add(
                    new Error.CSARErrorWrongSecurityFileName(pathToSourceFile, securityFilePath.getFileName().toString(), securityFileType)
                );
            } else if (!fileHaveOneOfExtensions(securityFilePath, securityFileExtensions)) {
                errors.add(
                    new Error.CSARErrorWrongSecurityFileExtension(pathToSourceFile, securityFileExtensions, securityFileType)
                );
            } else if (!securityFilePath.toFile().exists()) {
                errors.add(
                    new Error.CSARErrorUnableToFindArtifactSecurityFile(pathToSourceFile, pathToSecurityFile, securityFileType)
                );
            } else {
                validatedSecurityFilePath = securityFilePath;
            }
        }
        return new ValidatedSecurityFile(validatedSecurityFilePath,errors);
    }


}
