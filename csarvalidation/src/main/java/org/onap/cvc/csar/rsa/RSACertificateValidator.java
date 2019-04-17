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

package org.onap.cvc.csar.rsa;


import java.security.PublicKey;

public class RSACertificateValidator {

    private final X509RsaCertification x509RsaCertification;

    public RSACertificateValidator(X509RsaCertification x509RsaCertification) {
        this.x509RsaCertification = x509RsaCertification;
    }

    public boolean isValid(byte [] content, String signature, String publicCertificateContent) throws Exception {

        String publicCert = extractPublicKeyCertificate(publicCertificateContent);
        final PublicKey publicKey = this.x509RsaCertification.generatePublicKey(publicCert);

        return this.x509RsaCertification.verify(content,signature,publicKey);
    }

    private String extractPublicKeyCertificate(String publicCertificateContent) {
        String publicCert = publicCertificateContent.replace("-----BEGIN CERTIFICATE-----\n", "");
        return publicCert.replace("-----END CERTIFICATE-----\n", "");
    }
}
