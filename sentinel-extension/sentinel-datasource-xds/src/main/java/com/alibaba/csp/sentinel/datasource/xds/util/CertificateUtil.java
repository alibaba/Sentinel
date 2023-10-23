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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.expection.CertificateException;
import com.alibaba.csp.sentinel.log.RecordLog;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

/**
 * @author lwj
 * @since 2.0.0
 */
public final class CertificateUtil {

    private static final String PEM_CERTIFICATE_START = "-----BEGIN CERTIFICATE-----";

    private static final String PEM_CERTIFICATE_END = "-----END CERTIFICATE-----";

    private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";

    private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

    private CertificateUtil() {

    }

    public static KeyPair genKeyPair(AsymCryptoType asymCryptoType) {
        KeyPairGenerator localKeyPairGenerator = null;
        try {
            switch (asymCryptoType.getPrimaryType()) {
                case RSA:
                    localKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    localKeyPairGenerator.initialize(Integer.parseInt(asymCryptoType.getSubType()));
                    break;
                case ECDSA:
                    ECGenParameterSpec ecSpec = new ECGenParameterSpec(asymCryptoType.getSubType());
                    localKeyPairGenerator = KeyPairGenerator.getInstance("EC");
                    localKeyPairGenerator.initialize(ecSpec, new SecureRandom());
                    break;
                default:
                    throw new CertificateException("Unsupported asymmetric algorithm type");
            }
        } catch (Exception e) {
            throw new CertificateException("Unsupported genKeyPair", e);
        }
        KeyPair localKeyPair = localKeyPairGenerator.genKeyPair();
        return localKeyPair;
    }

    public static byte[] getRawPrivateKey(PrivateKey privateKey, AsymCryptoType asymCryptoType) {
        try {
            PemObject pemObject = new PemObject(asymCryptoType.getPrimaryType().getPrivateKeyHeader() + " PRIVATE KEY",
                privateKey.getEncoded());
            StringWriter str = new StringWriter();
            JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(str);
            jcaPEMWriter.writeObject(pemObject);
            jcaPEMWriter.close();
            str.close();
            return str.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CertificateException("Unable to parse raw private key");
        }

    }

    public static Certificate loadCertificate(String certificatePem) {
        try {
            CertificateFactory certificateFactory = CertificateFactory
                .getInstance("X509");
            certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_START, "");
            certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_END, "");
            certificatePem = certificatePem.replaceAll("\\s*", "");
            return certificateFactory.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));
        } catch (Exception e) {
            RecordLog.error("[XdsDataSource] Load certificate failed from pem string", e);
        }
        return null;
    }

}
