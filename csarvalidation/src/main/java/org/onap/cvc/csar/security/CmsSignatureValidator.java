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

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class CmsSignatureValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CmsSignatureValidator.class);

    public boolean verifySignedData(
            final byte[] cmsSignature,
            final Optional<byte[]> certificate,
            final byte[] fileContent) throws CmsSignatureValidatorException {

        try {
            CmsSignatureData signatureData = new CmsSignatureDataFactory().createForFirstSigner(cmsSignature, fileContent);
            if( !signatureData.containsCertificate()) {
                signatureData.loadCertificate(certificate.orElseThrow(() -> new CmsSignatureValidatorException("No certificate found in cms signature and ETSI-Entry-Certificate doesn't exist")));
            }
            return verifySignedData(signatureData);
        } catch ( IOException | CmsSignatureLoadingException e) {
            throw new CmsSignatureValidatorException("Unexpected error occurred during signature validation!", e);
        }
    }

    public boolean verifySignedData(final CmsSignatureData signatureData) throws CmsSignatureValidatorException {
        try {
            X509Certificate certificate = signatureData.getCertificate().orElseThrow(() -> new CMSException("No certificate found in signature data!"));
            return signatureData.getSignerInformation().verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate));
        } catch (CMSSignerDigestMismatchException e){
            //message-digest attribute value does not match calculated value
            LOG.warn("CMS signer digest mismatch.", e);
            return false;
        }
        catch (OperatorCreationException | CMSException e) {
            throw new CmsSignatureValidatorException("Unexpected error occurred during signature validation!", e);
        }
    }

}

