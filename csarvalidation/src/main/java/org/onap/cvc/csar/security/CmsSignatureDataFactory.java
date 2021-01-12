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
import org.bouncycastle.util.Store;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public class CmsSignatureDataFactory {

    private final SignatureFactory signatureFactory = new SignatureFactory();

    public CmsSignatureData createForFirstSigner(final byte[] cmsSignature, final byte[] fileContent)
        throws CmsSignatureLoadingException {

        try {
            CMSSignedData signedData = getCMSSignedData(fileContent, cmsSignature);
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            Store<X509CertificateHolder> certificates = signedData.getCertificates();
            SignerInformation firstSigner = getFirstSigner(signers);
            CmsSignatureData signatureData = new CmsSignatureData(firstSigner);
            getFirstSignerCertificate(certificates, firstSigner).ifPresent(
                signatureData::loadCertificate
            );
            return signatureData;
        } catch (CertificateLoadingException | IOException | CMSException e) {
            throw new CmsSignatureLoadingException("Unexpected error occurred during signature validation!", e);
        }
    }

    private SignerInformation getFirstSigner(Collection<SignerInformation> signers) {
        return signers.iterator().next();
    }

    private Optional<byte[]> getFirstSignerCertificate(
        Store<X509CertificateHolder> certificates,
        SignerInformation firstSigner)
        throws IOException {
        Collection<X509CertificateHolder> firstSignerCertificates = certificates.getMatches(firstSigner.getSID());
        Optional<byte[]> cert;
        if (!firstSignerCertificates.isEmpty()) {
            X509CertificateHolder firstSignerFirstCertificate = firstSignerCertificates.iterator().next();
            cert = Optional.of(firstSignerFirstCertificate.getEncoded());
        } else {
            cert = Optional.empty();
        }
        return cert;
    }

    private CMSSignedData getCMSSignedData(byte[] innerPackageFileCSAR, byte[] signatureStream) throws IOException, CmsSignatureLoadingException, CMSException {
        ContentInfo signature = signatureFactory.createSignature(signatureStream);
        CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFileCSAR);
        return new CMSSignedData(signedContent, signature);
    }

}
