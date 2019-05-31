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
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class CmsSignatureValidator {

    public boolean verifySignedData(
            final byte[] signature,
            final byte[] certificate,
            final byte[] csarFileContent) throws CmsSignatureValidatorException {

        try (ByteArrayInputStream signatureStream = new ByteArrayInputStream(signature)) {
            SignerInformation firstSigner = getSignerInformation(csarFileContent, signatureStream);
            X509Certificate cert = loadCertificate(certificate);

            return firstSigner.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (CMSSignerDigestMismatchException e){
            //message-digest attribute value does not match calculated value
            return false;
        }
        catch (OperatorCreationException | IOException | CMSException e) {
            throw new CmsSignatureValidatorException("Unexpected error occurred during signature validation!", e);
        }
    }

    private SignerInformation getSignerInformation(byte[] innerPackageFileCSAR, ByteArrayInputStream signatureStream) throws IOException, CmsSignatureValidatorException, CMSException {
        ContentInfo signature = produceSignature(signatureStream);
        CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFileCSAR);
        CMSSignedData signedData = new CMSSignedData(signedContent, signature);

        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        return signers.iterator().next();
    }

    private ContentInfo produceSignature(ByteArrayInputStream signatureStream) throws IOException, CmsSignatureValidatorException {
        Object parsedObject = new PEMParser(new InputStreamReader(signatureStream)).readObject();
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

