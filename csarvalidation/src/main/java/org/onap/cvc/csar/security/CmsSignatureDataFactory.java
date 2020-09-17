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

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Optional;

public class CmsSignatureDataFactory {

    public CmsSignatureData createForFirstSigner(final byte[] cmsSignature, final byte[] fileContent)
    throws CmsSignatureLoadingException{

        try (ByteArrayInputStream cmsSignatureStream = new ByteArrayInputStream(cmsSignature)) {
            CMSSignedData signedData = getCMSSignedData(fileContent, cmsSignatureStream);
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            Store<X509CertificateHolder> certificates = signedData.getCertificates();
            SignerInformation firstSigner = getFirstSigner(signers);
            Optional<X509Certificate> cert = getFirstSignerCertificate(certificates, firstSigner);
            return new CmsSignatureData(cert, firstSigner);
        } catch ( IOException | CMSException e) {
            throw new CmsSignatureLoadingException("Unexpected error occurred during signature validation!", e);
        }
    }

    private SignerInformation getFirstSigner(Collection<SignerInformation> signers) {
        SignerInformation firstSigner = signers.iterator().next();
        return firstSigner;
    }

    private Optional<X509Certificate> getFirstSignerCertificate(
        Store<X509CertificateHolder> certificates,
        SignerInformation firstSigner)
        throws CmsSignatureLoadingException, IOException {
        Collection<X509CertificateHolder> firstSignerCertificates = certificates.getMatches(firstSigner.getSID());
        Optional<X509Certificate> cert;
        if (!firstSignerCertificates.isEmpty()) {
            X509CertificateHolder firstSignerFirstCertificate = firstSignerCertificates.iterator().next();
            cert = Optional.of(loadCertificate(firstSignerFirstCertificate.getEncoded()));
        } else {
            cert = Optional.empty();
        }
        return cert;
    }


    private CMSSignedData getCMSSignedData(byte[] innerPackageFileCSAR, ByteArrayInputStream signatureStream) throws IOException, CmsSignatureLoadingException, CMSException {
        ContentInfo signature = produceSignature(signatureStream);
        CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFileCSAR);
        return new CMSSignedData(signedContent, signature);
    }

    private ContentInfo produceSignature(ByteArrayInputStream signatureStream) throws IOException, CmsSignatureLoadingException {
        Object parsedObject = new PEMParser(new InputStreamReader(signatureStream, Charset.defaultCharset())).readObject();
        if (!(parsedObject instanceof ContentInfo)) {
            throw new CmsSignatureLoadingException("Signature is not recognized!");
        }
        return ContentInfo.getInstance(parsedObject);
    }

    private X509Certificate loadCertificate(byte[] certFile) throws CmsSignatureLoadingException {
        try (InputStream in = new ByteArrayInputStream(certFile)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        } catch (CertificateException | IOException e) {
            throw new CmsSignatureLoadingException("Error during loading Certificate from bytes!", e);
        }
    }
}
