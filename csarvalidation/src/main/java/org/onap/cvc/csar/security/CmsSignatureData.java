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

    private Optional<X509Certificate> certificate;
    private SignerInformation signerInformation;

    public CmsSignatureData(Optional<X509Certificate> certificate, SignerInformation signerInformation) {
        this.certificate = certificate;
        this.signerInformation = signerInformation;
    }

    public boolean containsCertificate() {
        return certificate.isPresent();
    }

    public Optional<X509Certificate> getCertificate() {
        return certificate;
    }

    public SignerInformation getSignerInformation() {
        return signerInformation;
    }

    public void loadCertificate(Path pathToCertificate) throws IOException {
        loadCertificate(Files.readAllBytes(pathToCertificate));
    }

    public void loadCertificate(final byte[] certificate) throws IOException {
        try (InputStream in = new ByteArrayInputStream(certificate)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            this.certificate =  Optional.of( (X509Certificate) factory.generateCertificate(in) );
        } catch (CertificateException e) {
            throw new IOException("Error during loading Certificate from bytes!", e);
        }
    }
}
