/*
 * Copyright 2019
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
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class CmsSignatureValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CmsSignatureValidator.class);

    public boolean verifySignedData(
            final byte[] cmsSignature,
            final Optional<byte[]> certificate,
            final byte[] fileContent) throws CmsSignatureValidatorException {

        try (ByteArrayInputStream cmsSignatureStream = new ByteArrayInputStream(cmsSignature)) {
            CMSSignedData signedData = getCMSSignedData(fileContent, cmsSignatureStream);
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation firstSigner = signers.iterator().next();

            Store<X509CertificateHolder> certificates = signedData.getCertificates();
            Collection<X509CertificateHolder> firstSignerCertificates = certificates.getMatches(firstSigner.getSID());
            X509Certificate cert;
            if (!firstSignerCertificates.isEmpty()) {
                X509CertificateHolder firstSignerFirstCertificate = getX509CertificateHolder(firstSignerCertificates);
                cert = loadCertificate(firstSignerFirstCertificate.getEncoded());
            } else {
                cert = loadCertificate(certificate.orElseThrow(() -> new CmsSignatureValidatorException("No certificate found in cms signature and ETSI-Entry-Certificate doesn't exist")));
            }

            return firstSigner.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (CMSSignerDigestMismatchException e){
            //message-digest attribute value does not match calculated value
            LOG.warn("CMS signer digest mismatch.", e);
            return false;
        }
        catch (OperatorCreationException | IOException | CMSException e) {
            throw new CmsSignatureValidatorException("Unexpected error occurred during signature validation!", e);
        }
    }

    public boolean doesCmsContainsCertificate(
        final byte[] cmsSignature,
        final byte[] fileContent) {

        try (ByteArrayInputStream cmsSignatureStream = new ByteArrayInputStream(cmsSignature)) {
            CMSSignedData signedData = getCMSSignedData(fileContent, cmsSignatureStream);
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation firstSigner = signers.iterator().next();

            Store<X509CertificateHolder> certificates = signedData.getCertificates();
            Collection<X509CertificateHolder> firstSignerCertificates = certificates.getMatches(firstSigner.getSID());
            return !firstSignerCertificates.isEmpty();

        } catch (IOException | CmsSignatureValidatorException | CMSException e){
            //message-digest attribute value does not match calculated value
            LOG.error("Fail to lad CMS data.", e);
            return false;
        }
    }

    private X509CertificateHolder getX509CertificateHolder(Collection<X509CertificateHolder> firstSignerCertificates) throws CmsSignatureValidatorException {
        if(!firstSignerCertificates.iterator().hasNext()){
            throw new CmsSignatureValidatorException("No certificate found in cms signature that should contain one!");
        }
        return firstSignerCertificates.iterator().next();
    }

    private CMSSignedData getCMSSignedData(byte[] innerPackageFileCSAR, ByteArrayInputStream signatureStream) throws IOException, CmsSignatureValidatorException, CMSException {
        ContentInfo signature = produceSignature(signatureStream);
        CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFileCSAR);
        return new CMSSignedData(signedContent, signature);
    }

    private ContentInfo produceSignature(ByteArrayInputStream signatureStream) throws IOException, CmsSignatureValidatorException {
        Object parsedObject = new PEMParser(new InputStreamReader(signatureStream, Charset.defaultCharset())).readObject();
        if (!(parsedObject instanceof ContentInfo)) {
            throw new CmsSignatureValidatorException("Signature is not recognized!");
        }
        return ContentInfo.getInstance(parsedObject);
    }


    private X509Certificate loadCertificate(byte[] certFile) throws CmsSignatureValidatorException {
        try (InputStream in = new ByteArrayInputStream(certFile)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        } catch (CertificateException | IOException e) {
            throw new CmsSignatureValidatorException("Error during loading Certificate from bytes!", e);
        }
    }


}

