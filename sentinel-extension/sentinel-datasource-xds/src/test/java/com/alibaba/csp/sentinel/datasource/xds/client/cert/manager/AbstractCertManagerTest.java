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

import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.expection.CertificateException;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.trust.cert.CertPair;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author lwj
 * @since 2.0.0
 */
public class AbstractCertManagerTest {
    private static long EXPIRE_TIME = 10000;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test the certificate rotation
     */
    @Test
    public void testGetCertPair() throws InterruptedException {
        XdsConfigProperties xdsConfigProperties = XdsConfigProperties.getXdsDefaultXdsProperties();
        CertPairRepository certPairRepository = new CertPairRepository();
        MockCertManager mockCertManager = new MockCertManager(xdsConfigProperties, certPairRepository);
        CertPair certPair1 = mockCertManager.getCertPair();
        CertPair certPair2 = mockCertManager.getCertPair();

        assertEquals(certPair1, certPair2);

        Thread.sleep((long) (EXPIRE_TIME * xdsConfigProperties.getCertPeriodRatio()) + 1000);

        CertPair certPair3 = mockCertManager.getCertPair();
        CertPair certPair4 = mockCertManager.getCertPair();

        assertEquals(certPair3, certPair4);

        Thread.sleep((long) (EXPIRE_TIME * xdsConfigProperties.getCertPeriodRatio()) + 1000);

        CertPair certPair5 = mockCertManager.getCertPair();
        CertPair certPair6 = mockCertManager.getCertPair();

        assertEquals(certPair5, certPair6);

        assertNotSame(certPair1, certPair3);
        assertNotSame(certPair3, certPair5);
        mockCertManager.close();
    }

    @Test
    public void testNullCertPair() throws InterruptedException {
        exception.expect(CertificateException.class);
        exception.expectMessage("Without any certificate");
        XdsConfigProperties xdsConfigProperties = XdsConfigProperties.getXdsDefaultXdsProperties();
        CertPairRepository certPairRepository = new CertPairRepository();
        NullCertManager nullCertManager = new NullCertManager(xdsConfigProperties, certPairRepository);
        Thread.sleep(12000);
        assertEquals(3, nullCertManager.num);
        nullCertManager.getCertPair();

    }

    public static class MockCertManager extends AbstractCertManager {

        public MockCertManager(XdsConfigProperties xdsConfigProperties, CertPairRepository certPairRepository) {
            super(xdsConfigProperties, certPairRepository);
        }

        @Override
        protected CertPair doGetNewCertPair() {
            CertPair certPair = new CertPair();
            certPair.setExpireTime(System.currentTimeMillis() + EXPIRE_TIME);
            return certPair;
        }
    }

    public static class NullCertManager extends AbstractCertManager {
        public int num = 0;

        public NullCertManager(XdsConfigProperties xdsConfigProperties, CertPairRepository certPairRepository) {
            super(xdsConfigProperties, certPairRepository);
        }

        @Override
        protected CertPair doGetNewCertPair() {
            num += 1;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return null;
        }
    }

}