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
package com.alibaba.csp.sentinel.datasource.xds.client.cert.manager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyPair;

import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.HashType;
import com.alibaba.csp.sentinel.datasource.xds.expection.CertificateException;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.datasource.xds.util.CertificateUtil;
import com.alibaba.csp.sentinel.datasource.xds.util.TestUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;

/**
 * @author lwj
 * @since 2.0.0
 */
public class IstioCertManagerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * The test generated the CSR correctly
     */
    @Test
    public void testGetCSR() {
        for (AsymCryptoType asymCryptoType : AsymCryptoType.values()) {
            KeyPair keyPair = CertificateUtil.genKeyPair(asymCryptoType);
            for (HashType hashType : HashType.values()) {
                String csr = IstioCertManager.getCSR(keyPair, asymCryptoType, hashType);
                assertNotNull(csr);
            }

        }
    }

    /**
     * To test the creation of a channel, configure a correct ip address in /etc/host
     * And the correct ca certificate
     */
/*    @Test
    public void testGetManagedChannel() throws IOException, URISyntaxException {
        String caAddr = "istiod.istio-system.svc:15012";
        URL certPath = this.getClass().getClassLoader().getResource("root-cert.pem");
        ManagedChannel channel1 = IstioCertManager.getManagedChannel(
            FileUtil.readFileToString(new File(certPath.toURI()), StandardCharsets.UTF_8), caAddr);
        ManagedChannel channel2 = IstioCertManager.getManagedChannel(null, caAddr);
        ManagedChannel channel3 = IstioCertManager.getManagedChannel("", caAddr);
        assertNotNull(channel1);
        assertNotNull(channel2);
        assertNotNull(channel3);
        channel1.shutdown();
        channel2.shutdown();
        channel3.shutdown();
    }*/

    /**
     * This section describes how to test istio certificate management and
     *
     * @throws URISyntaxException
     * @throws IOException
     */
/*    @Test
    public void testIstioCertManager() throws URISyntaxException, IOException, CertificateParsingException {
        CertPairRepository certPairRepository = new CertPairRepository();
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        IstioCertManager istioCertManager = new IstioCertManager(xdsConfigProperties, certPairRepository);
        CertPair certPair = certPairRepository.getInstance();
        assertNotNull(certPair.getRawCertificateChain());
        assertNotNull(certPair.getCertificateChain());
        X509Certificate x509Certificate = (X509Certificate) (certPair.getCertificateChain()[0]);
        assertTrue(StringUtil.isNotEmpty(x509Certificate.getSubjectDN().toString()));
        Collection<List<?>> san = x509Certificate.getSubjectAlternativeNames();
        String principle = (String) san.iterator().next().get(1);
        assertNotNull(StringUtil.isNotEmpty(principle));
        assertNotNull(certPair.getPrivateKey());
        assertNotNull(certPair.getRawPrivateKey());
        assertNotNull(certPair.getRootCA());
        assertTrue(
            certPair.getExpireTime() <= System.currentTimeMillis() + xdsConfigProperties.getCertValidityTimeS() * 1000);
        assertTrue((System.currentTimeMillis() + (xdsConfigProperties.getCertValidityTimeS() - 10) * 1000)
            <= certPair.getExpireTime());
        istioCertManager.close();
    }*/
    @Test
    public void testNullIstioCertManager() throws URISyntaxException, IOException {
        exception.expect(CertificateException.class);
        exception.expectMessage("Without any certificate");
        CertPairRepository certPairRepository = new CertPairRepository();
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        xdsConfigProperties.setCaAddr("www.abc.com:11");
        xdsConfigProperties.setGetCertTimeoutS(5);
        IstioCertManager istioCertManager = new IstioCertManager(xdsConfigProperties, certPairRepository);
        istioCertManager.getCertPair();
    }

}