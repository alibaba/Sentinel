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
package com.alibaba.csp.sentinel.datasource.xds.client.cert.channel;

import io.envoyproxy.envoy.config.core.v3.Node;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author lwj
 * @since 2.0.0
 */
public class XdsChannelTest {

    @Test
    public void testCreateNode() {
        Node node = XdsChannel.createNode("abc", "default", "Kubernetes");
        assertNotNull(node);

    }

    @Test
    public void testCreateNodeId() {
        String nodeId = XdsChannel.createNodeId("abc", "default");
        assertNotNull(nodeId);
    }

/*    @Test
    public void testRestart() throws URISyntaxException, IOException {
        CertPairRepository certPairRepository = new CertPairRepository();
        XdsConfigProperties xdsConfigProperties = TestUtil.createConfig();
        AbstractCertManager abstractCertManager = new IstioCertManager(xdsConfigProperties, certPairRepository);
        XdsChannel xdsChannel = new XdsChannel(xdsConfigProperties, abstractCertManager);
        ManagedChannel channel = xdsChannel.getChannel();
        assertNotNull(channel);
        Node node = xdsChannel.getNode();
        assertNotNull(node);
        xdsChannel.restart();
        xdsChannel.close();
    }*/

}