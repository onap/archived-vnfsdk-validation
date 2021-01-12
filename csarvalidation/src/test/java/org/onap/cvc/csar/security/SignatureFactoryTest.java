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

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureFactoryTest {

    private final static String testPemSignature = "" +
        "-----BEGIN CMS-----\n" +
        "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" +
        "dDrh3CbBGa9nLr6i1jOOp1TxYM8/e2kYbVIl" +
        "\n-----END CMS-----";

    private final static String testPkcs7Signature = "" +
        "-----BEGIN PKCS7-----\n" +
        "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" +
        "dDrh3CbBGa9nLr6i1jOOp1TxYM8/e2kYbVIl" +
        "\n-----END PKCS7-----";

    private SignatureFactory signatureFactory;

    @Before
    public void setUp(){
        signatureFactory = new SignatureFactory();
    }

    @Test
    public void shouldCreateContentInfoWithProperContentForPEM() throws IOException, CmsSignatureLoadingException {

        // when
        ContentInfo contentInfo = signatureFactory.createSignature(testPemSignature.getBytes());

        // then
        final String contentInfoSignature = getContentInfoSignatureAsPem(contentInfo);
        assertThat(testPemSignature).contains(contentInfoSignature);
    }

    @Test
    public void shouldCreateContentInfoWithProperContentForPKCS7() throws IOException, CmsSignatureLoadingException {

        // when
        ContentInfo contentInfo = signatureFactory.createSignature(testPkcs7Signature.getBytes());

        // then
        final String contentInfoSignature = getContentInfoSignatureAsPem(contentInfo);
        assertThat(testPkcs7Signature).contains(contentInfoSignature);
    }

    @Test
    public void shouldCreateContentInfoWithProperContentForDecodedPKCS7() throws IOException, CmsSignatureLoadingException {
        // given
        InputStream signatureAsStream = loadFileFromResources("./security/signature/testEncodedSignature.sig.p7c");

        // when
        ContentInfo contentInfo = signatureFactory.createSignature(signatureAsStream.readAllBytes());

        // then
        final String contentInfoSignature = getContentInfoSignatureAsPem(contentInfo);
        assertThat(testPkcs7Signature).contains(contentInfoSignature);
    }

    private InputStream loadFileFromResources(String fileFromResources) throws IOException{
        InputStream resourceAsStream = SignatureFactoryTest.class.getClassLoader().getResourceAsStream(fileFromResources);
        if(resourceAsStream==null) {
            throw  new IOException(String.format("fail to load file: %s from resourcer", fileFromResources));
        }
        return resourceAsStream;
    }

    private String getContentInfoSignatureAsPem(ContentInfo contentInfo) throws IOException {
        return new String(Base64.getEncoder().encode(contentInfo.toASN1Primitive().getEncoded(ASN1Encoding.DER)));
    }

}
