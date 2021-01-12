/*
 * Copyright 2021 Nokia
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

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.openssl.PEMParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SignatureFactory {

    public static final String PEM_SIGNATURE_BEGIN_TAG = "-----BEGIN CMS-----";
    public static final String PEM_SIGNATURE_END_TAG = "-----END CMS-----";

    public ContentInfo createSignature(byte[] signatureStream) throws IOException, CmsSignatureLoadingException {
        byte[] pemSignatureStream = convertSignatureToPemIfInDerFormat(signatureStream);
        try(ByteArrayInputStream signatureInput = new ByteArrayInputStream(pemSignatureStream)) {
            Object parsedObject = new PEMParser(new InputStreamReader(signatureInput)).readPemObject().getContent();
            return ContentInfo.getInstance(parsedObject);
        } catch (Exception e) {
            throw new CmsSignatureLoadingException("Signature is not recognized!", e);
        }
    }

    private byte[] convertSignatureToPemIfInDerFormat(byte[] signatureStream) throws IOException {
        byte[] encodedSignatureStream = signatureStream.clone();
        if (!Base64.isBase64(signatureStream)) {
            encodedSignatureStream = Base64.encodeBase64(encodedSignatureStream);
            encodedSignatureStream = wrapWithPemTags(encodedSignatureStream);
        }
        return encodedSignatureStream;
    }

    private byte[] wrapWithPemTags(byte[] encodedSignatureStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( (PEM_SIGNATURE_BEGIN_TAG + "\n").getBytes() );
        outputStream.write( encodedSignatureStream );
        outputStream.write( ("\n" + PEM_SIGNATURE_END_TAG).getBytes() );
        return outputStream.toByteArray();
    }
}
