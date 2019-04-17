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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RSACertificateValidatorTest {

    @Mock
    private X509RsaCertification x509RsaCertification;

    @Mock
    private PublicKey publicKey;

    @Test
    public void shouldReturnInformationThatCsarHasValidSignature() throws Exception {

        // given
        String publicCertificate ="-----BEGIN CERTIFICATE-----\n" +
                "MIIDyzCCArMCCQCXF5To+FxujDANBgkqhkiG9w0BAQsFADCBrjELMAkGA1UEBhMC\n" +
                "SUUxETAPBgNVBAgMCExlaW5zdGVyMQ8wDQYDVQQHDAZEdWJsaW4xETAPBgNVBAoM\n" +
                "CEVyaWNzc29uMRwwGgYDVQQLDBNCdXNpbmVzcyBBcmVhIFJhZGlvMSMwIQYDVQQD\n" +
                "DBpSb290IGNlcnRpZmljYXRlIGF1dGhvcml0eTElMCMGCSqGSIb3DQEJARYWYXV0\n" +
                "aG9yaXR5QGVyaWNzc29uLmNvbTAeFw0xOTAzMDcyMDA4MDRaFw0xOTA0MDYyMDA4\n" +
                "MDRaMIGfMQswCQYDVQQGEwJJRTERMA8GA1UECAwITGVpbnN0ZXIxDzANBgNVBAcM\n" +
                "BkR1YmxpbjERMA8GA1UECgwIRXJpY3Nzb24xHDAaBgNVBAsME0J1c2luZXNzIEFy\n" +
                "ZWEgUmFkaW8xFzAVBgNVBAMMDlBhY2thZ2Ugc2lnbmVyMSIwIAYJKoZIhvcNAQkB\n" +
                "FhNzaWduZXJAZXJpY3Nzb24uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEA1bZWYbM3W9WK7E6brlMWw/pHdYmKrLmqnmyS4QWj6PoSudReX1x1QO+o\n" +
                "jlzzlWn15ozgeDtsyQWRQakSkV8IUlywmM99tH7jGejrH87eLYv0IoJONVJLMsuQ\n" +
                "chMd/cm0OGwUHHuk7iRnMGlcskp3FPvHlBRgBLrg+40yksJMmpHyS9amrG2/3bSa\n" +
                "ssuc3F8ICNtejYVXDg5rIHyKIvD8Jaozf+V8FyFcFkfL7NyIS8rSuHM40vp3jlVO\n" +
                "yNDztZ9orTA9Frucxr6y5UIXHd/bmh7YsjihyCoPOwvkfEy/S08S245eKS1zwgcE\n" +
                "zkSwPC+XR7HwXoVb63hgBlcJCkUAswIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCC\n" +
                "nWjpa+JeJj05UfX0tejdnHTotnT4AQfxp1YesG3O7ioIY4Y93/Cj8N+7rzeB392v\n" +
                "eUMN2HKXGNRZhVJKs8fdoD/b5OxlwX1BattPS1Oh7HmLYzevOxotrm5YOR4KG2qa\n" +
                "Rw/m6jFWxnAovpQTaCOgkuAJyF9l6wlQE4FyzyZMaThObcnLBzuQJjJXKMwaVT6D\n" +
                "AQuMP3DRrH3aXlFpqV4bugLy8agSc2w9sF3w4osGZSwPjerJiulncUyBr+cjv1KB\n" +
                "IfgzoP3b9frMBZmSpxeT3YzR1wZAh9AterRKAm6EGVxrnRDQ1b/OuW4y2RxQ/Q3G\n" +
                "OUU/dbcjLaFvoQsv3aAk\n" +
                "-----END CERTIFICATE-----\n";

        String signature = "r+18GjD74DWNbp1U5zzbw7lB0QI5OXXBReGQ5DmRn/SFqQj0H22omSoolqlmwk8fc6pBfSTQl68yWEztH6m14dKTcYozVFpn1TS0qSgxMYjPJ5N/4+wrhC/70yosLATdc2w1U/9UYeFxP0QbCBSLtH9dDgTfm8e7Y25c7l6jSI+/VZ6b4lno5786y4W/VYeP6ktOvI0qbLtFPLfpxjqJ5idXUspkblhrZ6dHzURTlUWfYTku5NfLoIPL2Hdr8WfTBBTk+TYmAEBGC7J3SY5m1SZOOGElh80CfLGFVtdZ862Sgj2X8hV1isBTEJpczQwdMmid2xzdmZgbnkzFh9F/eQ==";
        byte [] content = new byte[] {'t','e','s','t'};


        String cert = "MIIDyzCCArMCCQCXF5To+FxujDANBgkqhkiG9w0BAQsFADCBrjELMAkGA1UEBhMC\n" +
                "SUUxETAPBgNVBAgMCExlaW5zdGVyMQ8wDQYDVQQHDAZEdWJsaW4xETAPBgNVBAoM\n" +
                "CEVyaWNzc29uMRwwGgYDVQQLDBNCdXNpbmVzcyBBcmVhIFJhZGlvMSMwIQYDVQQD\n" +
                "DBpSb290IGNlcnRpZmljYXRlIGF1dGhvcml0eTElMCMGCSqGSIb3DQEJARYWYXV0\n" +
                "aG9yaXR5QGVyaWNzc29uLmNvbTAeFw0xOTAzMDcyMDA4MDRaFw0xOTA0MDYyMDA4\n" +
                "MDRaMIGfMQswCQYDVQQGEwJJRTERMA8GA1UECAwITGVpbnN0ZXIxDzANBgNVBAcM\n" +
                "BkR1YmxpbjERMA8GA1UECgwIRXJpY3Nzb24xHDAaBgNVBAsME0J1c2luZXNzIEFy\n" +
                "ZWEgUmFkaW8xFzAVBgNVBAMMDlBhY2thZ2Ugc2lnbmVyMSIwIAYJKoZIhvcNAQkB\n" +
                "FhNzaWduZXJAZXJpY3Nzb24uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEA1bZWYbM3W9WK7E6brlMWw/pHdYmKrLmqnmyS4QWj6PoSudReX1x1QO+o\n" +
                "jlzzlWn15ozgeDtsyQWRQakSkV8IUlywmM99tH7jGejrH87eLYv0IoJONVJLMsuQ\n" +
                "chMd/cm0OGwUHHuk7iRnMGlcskp3FPvHlBRgBLrg+40yksJMmpHyS9amrG2/3bSa\n" +
                "ssuc3F8ICNtejYVXDg5rIHyKIvD8Jaozf+V8FyFcFkfL7NyIS8rSuHM40vp3jlVO\n" +
                "yNDztZ9orTA9Frucxr6y5UIXHd/bmh7YsjihyCoPOwvkfEy/S08S245eKS1zwgcE\n" +
                "zkSwPC+XR7HwXoVb63hgBlcJCkUAswIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCC\n" +
                "nWjpa+JeJj05UfX0tejdnHTotnT4AQfxp1YesG3O7ioIY4Y93/Cj8N+7rzeB392v\n" +
                "eUMN2HKXGNRZhVJKs8fdoD/b5OxlwX1BattPS1Oh7HmLYzevOxotrm5YOR4KG2qa\n" +
                "Rw/m6jFWxnAovpQTaCOgkuAJyF9l6wlQE4FyzyZMaThObcnLBzuQJjJXKMwaVT6D\n" +
                "AQuMP3DRrH3aXlFpqV4bugLy8agSc2w9sF3w4osGZSwPjerJiulncUyBr+cjv1KB\n" +
                "IfgzoP3b9frMBZmSpxeT3YzR1wZAh9AterRKAm6EGVxrnRDQ1b/OuW4y2RxQ/Q3G\n" +
                "OUU/dbcjLaFvoQsv3aAk\n";

        when(x509RsaCertification.generatePublicKey(cert)).thenReturn(publicKey);
        when(x509RsaCertification.verify(content,signature, publicKey)).thenReturn(true);

        // when
        RSACertificateValidator rsaCertificateValidator = new RSACertificateValidator(x509RsaCertification);

        // then
        assertThat(rsaCertificateValidator.isValid(content, signature, publicCertificate)).isTrue();
        verify(x509RsaCertification,times(1)).generatePublicKey(cert);
        verify(x509RsaCertification,times(1)).verify(content,signature, publicKey);
    }

}