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
package com.alibaba.csp.sentinel.datasource.xds.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds.AuthLdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds.TlsModeLdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.expection.XdsException;
import com.alibaba.csp.sentinel.datasource.xds.property.XdsProperty;
import com.alibaba.csp.sentinel.datasource.xds.util.TestUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author lwj
 * @since 2.0.0
 */
public class XdsClientTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

/*    @Test
    public void testClient() throws URISyntaxException, IOException, InterruptedException {
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        XdsProperty xdsProperty = new XdsProperty();
        AuthLdsFilter authLdsFilter = new AuthLdsFilter(xdsProperty.getAuthRepository());
        TlsModeLdsFilter tlsModeRepository = new TlsModeLdsFilter(xdsProperty.getTlsModeRepository());
        XdsClient xdsClient = new XdsClient(xdsConfigProperties, Arrays.asList(authLdsFilter, tlsModeRepository),
            xdsProperty.getCertPairRepository());
        xdsClient.start();
        xdsClient.restart();
        xdsClient.close();
    }*/

    @Test
    public void testErrClient() throws URISyntaxException, IOException {
        exception.expect(XdsException.class);
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        xdsConfigProperties.setCaAddr("www.abc.com:11");
        xdsConfigProperties.setGetCertTimeoutS(10);
        XdsProperty xdsProperty = new XdsProperty();
        AuthLdsFilter authLdsFilter = new AuthLdsFilter(xdsProperty.getAuthRepository());
        TlsModeLdsFilter tlsModeRepository = new TlsModeLdsFilter(xdsProperty.getTlsModeRepository());
        XdsClient xdsClient = new XdsClient(xdsConfigProperties, Arrays.asList(authLdsFilter, tlsModeRepository),
            xdsProperty.getCertPairRepository());
    }

/*    @Test
    public void testClientCert() throws URISyntaxException, IOException, InterruptedException {
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        xdsConfigProperties.setCertValidityTimeS(10);
        XdsProperty xdsProperty = new XdsProperty();
        AuthLdsFilter authLdsFilter = new AuthLdsFilter(xdsProperty.getAuthRepository());
        TlsModeLdsFilter tlsModeRepository = new TlsModeLdsFilter(xdsProperty.getTlsModeRepository());
        XdsClient xdsClient = new XdsClient(xdsConfigProperties, Arrays.asList(authLdsFilter, tlsModeRepository),
            xdsProperty.getCertPairRepository());
        xdsClient.start();
        TlsMode tlsModeOri = xdsProperty.getTlsModeRepository().getInstance();
        xdsProperty.getTlsModeRepository().update(null);
        Thread.sleep(10000);
        TlsMode tlsModeNew = xdsProperty.getTlsModeRepository().getInstance();
        assertEquals(tlsModeOri, tlsModeNew);
        xdsClient.close();
    }*/
}