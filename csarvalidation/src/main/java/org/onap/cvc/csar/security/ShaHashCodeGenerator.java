/*
 * Copyright 2019 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.cvc.csar.security;

import org.bouncycastle.util.encoders.Hex;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShaHashCodeGenerator {

    public String generateSha256(byte[] source) throws NoSuchAlgorithmException {
        final String algorithm = "SHA-256";
        return generateHashCode(source, algorithm);
    }

    public String generateSha512(byte[] source) throws NoSuchAlgorithmException {
        final String algorithm = "SHA-512";
        return generateHashCode(source, algorithm);
    }

    private String generateHashCode(byte[] source, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(source);
        return new String(Hex.encode(hash));
    }
}
