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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509RsaCertification {

    private static final Logger LOG = LoggerFactory.getLogger(X509RsaCertification.class);

    PublicKey generatePublicKey(String cert) throws CertificateException {
        byte[] encodedCert = cert.getBytes(StandardCharsets.UTF_8);
        byte[] decodedCert = Base64.decodeBase64(encodedCert);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(decodedCert);
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);

        LOG.info(String.format("Subject DN : %s", certificate.getSubjectDN().getName()));
        LOG.info(String.format("Issuer : %s", certificate.getIssuerDN().getName()));
        LOG.info(String.format("Not After: %s", certificate.getNotAfter()));
        LOG.info(String.format("Not Before: %s", certificate.getNotBefore()));
        LOG.info(String.format("version: %d", certificate.getVersion()));
        LOG.info(String.format("serial number : %s", certificate.getSerialNumber()));

        return certificate.getPublicKey();
    }

    boolean verify(byte[] content, String signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(content);

        byte[] signatureBytes = java.util.Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }
}
