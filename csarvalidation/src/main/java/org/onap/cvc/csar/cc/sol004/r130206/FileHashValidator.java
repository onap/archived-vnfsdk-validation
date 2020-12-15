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

package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.security.ShaHashCodeGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class FileHashValidator {

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_512 = "SHA-512";

    private final ShaHashCodeGenerator shaHashCodeGenerator = new ShaHashCodeGenerator();

    public boolean isValid(String expectedHash, Path fileToCheck, String algorithm)
        throws NoSuchAlgorithmException, IOException {
        String hashCode = generateHashCode(fileToCheck, algorithm);
        return hashCode.equals(expectedHash);
    }

    private String generateHashCode(Path fileToCheck,  String algorithm)
        throws NoSuchAlgorithmException, IOException {
        final byte[] sourceData = Files.readAllBytes(fileToCheck);

        if (algorithm.equalsIgnoreCase(SHA_256)) {
            return this.shaHashCodeGenerator.generateSha256(sourceData);
        } else if (algorithm.equalsIgnoreCase(SHA_512)) {
            return this.shaHashCodeGenerator.generateSha512(sourceData);
        }

        throw new UnsupportedOperationException(String.format("Algorithm '%s' is not supported!", algorithm));
    }

}
