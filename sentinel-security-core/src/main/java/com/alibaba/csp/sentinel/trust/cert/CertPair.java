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
package com.alibaba.csp.sentinel.trust.cert;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Objects;

/**
 * Certificate.
 *
 * @author lwj
 * @since 2.0.0
 */
public class CertPair {

    private Certificate[] certificateChain;

    private PrivateKey privateKey;

    private byte[] rawCertificateChain;

    private byte[] rawPrivateKey;

    private Certificate rootCA;

    /**
     * Expiration time: certificate becomes invalid after this time
     */
    private long expireTime;

    public CertPair() {

    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public byte[] getRawCertificateChain() {
        return rawCertificateChain;
    }

    public void setRawCertificateChain(byte[] rawCertificateChain) {
        this.rawCertificateChain = rawCertificateChain;
    }

    public byte[] getRawPrivateKey() {
        return rawPrivateKey;
    }

    public void setRawPrivateKey(byte[] rawPrivateKey) {
        this.rawPrivateKey = rawPrivateKey;
    }

    public Certificate getRootCA() {
        if (rootCA == null) {
            return certificateChain[certificateChain.length - 1];
        }
        return rootCA;
    }

    public void setRootCA(Certificate rootCA) {
        this.rootCA = rootCA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertPair certPair = (CertPair) o;
        return expireTime == certPair.expireTime && Arrays.equals(certificateChain, certPair.certificateChain)
            && Objects.equals(privateKey, certPair.privateKey) && Arrays.equals(rawCertificateChain,
            certPair.rawCertificateChain) && Arrays.equals(rawPrivateKey, certPair.rawPrivateKey) && Objects.equals(
            rootCA, certPair.rootCA);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(privateKey, rootCA, expireTime);
        result = 31 * result + Arrays.hashCode(certificateChain);
        result = 31 * result + Arrays.hashCode(rawCertificateChain);
        result = 31 * result + Arrays.hashCode(rawPrivateKey);
        return result;
    }

    @Override
    public String toString() {
        return "CertPair{" +
            "certificateChain=" + Arrays.toString(certificateChain) +
            ", privateKey=" + privateKey +
            ", rawCertificateChain=" + Arrays.toString(rawCertificateChain) +
            ", rawPrivateKey=" + Arrays.toString(rawPrivateKey) +
            ", rootCA=" + rootCA +
            ", expireTime=" + expireTime +
            '}';
    }
}
