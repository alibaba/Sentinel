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
package com.alibaba.csp.sentinel.trust;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lwj
 * @since 2.0.0
 */
public class TrustManagerTest {

    @Test
    public void testStoreCertPair() {
        int[] s = {0};
        TrustManager.getInstance().registerCertCallback(cert -> s[0]++);
        for (int i = 0; i < 100; i++) {
            TrustManager.getInstance().storeCertPair(null);
        }
        Assert.assertEquals(100, s[0]);
    }

    @Test
    public void testStoreTlsMode() {
        int[] s = {0};
        TrustManager.getInstance().registerTlsModeCallback(tlsMode -> s[0]++);
        for (int i = 0; i < 100; i++) {
            TrustManager.getInstance().storeTlsMode(null);
        }
        Assert.assertEquals(100, s[0]);
    }

    @Test
    public void testStoreRules() {
        int[] s = {0};
        TrustManager.getInstance().registerRulesCallback(rules -> s[0]++);
        for (int i = 0; i < 100; i++) {
            TrustManager.getInstance().storeRules(null);
        }
        Assert.assertEquals(100, s[0]);
    }
}