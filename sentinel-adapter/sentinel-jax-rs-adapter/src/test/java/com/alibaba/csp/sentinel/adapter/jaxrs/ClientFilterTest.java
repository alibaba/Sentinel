/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.jaxrs;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.CtSph;
import com.alibaba.csp.sentinel.adapter.jaxrs.config.SentinelJaxRsConfig;
import com.alibaba.csp.sentinel.adapter.jaxrs.fallback.SentinelJaxRsFallback;
import com.alibaba.csp.sentinel.adapter.jaxrs.request.ResourceNameParser;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * @author sea
 */
public class ClientFilterTest {

    private static final String HELLO_STR = "Hello!";

    static int port;

    static String host;

    static Client client;

    static ConfigurableApplicationContext ctx;

    @BeforeClass
    public static void startApplication() {
        client = ClientBuilder.newClient()
                .register(new SentinelJaxRsClientFilter());

        port = SocketUtils.findAvailableTcpPort();
        host = "http://127.0.0.1:" + port;
        SpringApplication springApplication = new SpringApplication(TestApplication.class);
        ctx = springApplication.run("--server.port=" + port);
    }

    @AfterClass
    public static void shutdown() {
        ctx.close();

        Context context = ContextUtil.getContext();
        if (context != null) {
            context.setCurEntry(null);
            ContextUtil.exit();
        }

        Constants.ROOT.removeChildList();

        ClusterBuilderSlot.getClusterNodeMap().clear();

        // Clear chainMap in CtSph
        try {
            Method resetChainMapMethod = CtSph.class.getDeclaredMethod("resetChainMap");
            resetChainMapMethod.setAccessible(true);
            resetChainMapMethod.invoke(null);
        } catch (Exception e) {
            // Empty
        }
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }

    @Test
    public void testClientGetHello() {
        String url = "/test/hello";
        String resourceName = "GET:" + url;
        Response response = client.target(host).path(url).request()
                .get();
        assertEquals(200, response.getStatus());
        assertEquals(HELLO_STR, response.readEntity(String.class));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);

        String context = "";
        for (Node n : Constants.ROOT.getChildList()) {
            if (n instanceof EntranceNode) {
                String id = ((EntranceNode) n).getId().getName();
                if (url.equals(id)) {
                    context = ((EntranceNode) n).getId().getName();
                }
            }
        }
        assertEquals("", context);
    }

    @Test
    public void testClientAsyncGetHello() throws InterruptedException, ExecutionException {
        String url = "/test/async-hello";
        final String resourceName = "GET:" + url;

        Future<String> future = client.target(host).path(url).request()
                .async()
                .get(new InvocationCallback<String>() {
                    @Override
                    public void completed(String result) {

                    }

                    @Override
                    public void failed(Throwable throwable) {

                    }
                });

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNull(cn);
        assertEquals(HELLO_STR, future.get());

        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testCustomResourceNameParser() {
        String url = "/test/hello/{name}";
        String resourceName = "GET:" + url;

        SentinelJaxRsConfig.setResourceNameParser(new ResourceNameParser() {
            @Override
            public String parse(ContainerRequestContext containerRequestContext, ResourceInfo resourceInfo) {
                return null;
            }

            @Override
            public String parse(ClientRequestContext clientRequestContext) {
                String url = clientRequestContext.getUri().getPath();
                if (url.startsWith("/test/hello/")) {
                    return clientRequestContext.getMethod() + ":" + "/test/hello/{name}";
                }
                return clientRequestContext.getMethod() + ":" + url;
            }
        });

        Response response1 = client.target(host)
                .path(url)
                .resolveTemplate("name", "abc")
                .request()
                .get();
        assertEquals(200, response1.getStatus());
        assertEquals("Hello abc !", response1.readEntity(String.class));

        Response response2 = client.target(host)
                .path(url)
                .resolveTemplate("name", "def")
                .request()
                .get();
        assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response2.getStatus());
        assertEquals("Hello def !", response2.readEntity(String.class));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(2, cn.passQps(), 0.01);

        assertNull(ClusterBuilderSlot.getClusterNode("/test/hello/abc"));
        assertNull(ClusterBuilderSlot.getClusterNode("/test/hello/def"));
    }

    @Test
    public void testClientFallback() {
        String url = "/test/hello";
        String resourceName = "GET:" + url;
        configureRulesFor(resourceName, 0);

        Response response = client.target(host).path(url).request()
                .get();
        assertEquals(javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS.getStatusCode(), response.getStatus());
        assertEquals("Blocked by Sentinel (flow limiting)", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testClientCustomFallback() {
        String url = "/test/hello";
        String resourceName = "GET:" + url;
        configureRulesFor(resourceName, 0);

        SentinelJaxRsConfig.setJaxRsFallback(new SentinelJaxRsFallback() {
            @Override
            public javax.ws.rs.core.Response fallbackResponse(String route, Throwable cause) {
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.OK)
                        .entity("Blocked by Sentinel (flow limiting)")
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        });

        Response response = client.target(host).path(url).request()
                .get();
        assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Blocked by Sentinel (flow limiting)", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    private void configureRulesFor(String resource, int count) {
        configureRulesFor(resource, count, "default");
    }

    private void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
                .setCount(count)
                .setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
