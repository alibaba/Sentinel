/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.util;

import java.security.KeyPair;

import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author lwj
 * @since 2.0.0
 */
public class CertificateUtilTest {

    /**
     * Test the correctness of the asymmetric encryption algorithm
     */
    @Test
    public void testGenKeyPairAndCreateRawPrivateKey() {
        for (AsymCryptoType asymCryptoType : AsymCryptoType.values()) {
            KeyPair keyPair = CertificateUtil.genKeyPair(asymCryptoType);
            CertificateUtil.getRawPrivateKey(keyPair.getPrivate(), asymCryptoType);
            assertNotNull(keyPair);
        }
    }

    @Test
    public void testLoadCertificate() {
        String rawCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIICxTCCAa2gAwIBAgIQaCTS3IoYLCYSqcrinzipRTANBgkqhkiG9w0BAQsFADAY\n" +
            "MRYwFAYDVQQKEw1jbHVzdGVyLmxvY2FsMB4XDTIzMDcxOTA2NTAxNVoXDTIzMDcx\n" +
            "OTA2NTIyNVowADCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEArwtovkMhWBPV\n" +
            "XG5GCHknVsz2EvCL0uiE2/iqmYyI9DwKD0zq3a6QMXHPM8ryIB17awYADW/i651v\n" +
            "MJC2YFABOa0KUMC6Zgd6yq0jXIURArAzZguDCmJNswjVe7gYjQr4REIWmL8XYQ6f\n" +
            "uYL8oANQYEFxXPgBaAIduvu0Jz2UZbUCAwEAAaOBpjCBozAOBgNVHQ8BAf8EBAMC\n" +
            "BaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwGA1UdEwEB/wQCMAAw\n" +
            "HwYDVR0jBBgwFoAU15wMCaRyELhpHvlLFiBDtevA4nAwQwYDVR0RAQH/BDkwN4Y1\n" +
            "c3BpZmZlOi8vY2x1c3Rlci5sb2NhbC9ucy9kZWZhdWx0L3NhL2Jvb2tpbmZvLWRl\n" +
            "dGFpbHMwDQYJKoZIhvcNAQELBQADggEBADmB6AnmOzl0rQPdlRfmawqBOK4+oMVZ\n" +
            "QeDtIips1rAvwYRjL/eWMcXc/eeAfP8071EufkBkhBfplU0pGW7MnPlPu1H17+Mp\n" +
            "SIOUv6+KzCGyoRJL0Cdi13esWET2viHLna9RbGXdk7ROvxHRsPmW1+8jnVl+4j3g\n" +
            "ekllJrED7SUQI3EqUhlWAgEu/v+PhELbOEWc2gRPFriW0Wew9ckTnq/jJo9x1Ltj\n" +
            "+rMXf3Jh+3YxR8nYXVjawC8g1q/CJAVaxUumBFqC0Z2PXwakDIAIaWXNZZHTCo+R\n" +
            "287rWSxGExG91D7jG8pHAoYMp59gjMj+XFHmWqLCtv/Znx1sLd1i500=\n" +
            "-----END CERTIFICATE-----\n";
        assertNotNull(CertificateUtil.loadCertificate(rawCert));
    }
}