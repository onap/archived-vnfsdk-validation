/*
 * Copyright 2020 Nokia
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

import org.bouncycastle.cms.SignerInformation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class CmsSignatureData {

    private X509Certificate certificate;
    private final SignerInformation signerInformation;

    public CmsSignatureData(X509Certificate certificate, SignerInformation signerInformation) {
        this.certificate = certificate;
        this.signerInformation = signerInformation;
    }

    public CmsSignatureData(SignerInformation signerInformation) {
        this.signerInformation = signerInformation;
    }

    public Optional<X509Certificate> getCertificate() {
        return Optional.ofNullable(certificate);
    }

    public SignerInformation getSignerInformation() {
        return signerInformation;
    }

    public void loadCertificate(Path pathToCertificate) throws CertificateLoadingException {
        try {
            loadCertificate(Files.readAllBytes(pathToCertificate));
        } catch (IOException e) {
            final String errorMessage = String.format(
                "Error during loading Certificate from given path: %s !"
                ,pathToCertificate
            );
            throw new CertificateLoadingException(errorMessage, e);
        }
    }

    public void loadCertificate(final byte[] certificate) throws CertificateLoadingException {
        try (InputStream in = new ByteArrayInputStream(certificate)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            this.certificate =  (X509Certificate) factory.generateCertificate(in);
        } catch (IOException | CertificateException e) {
            throw new CertificateLoadingException("Error during loading Certificate from bytes!", e);
        }
    }

}
