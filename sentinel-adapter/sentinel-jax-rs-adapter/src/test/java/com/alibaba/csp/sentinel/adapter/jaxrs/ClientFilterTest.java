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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.*;

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
        client = ClientBuilder.newBuilder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        port = SocketUtils.findAvailableTcpPort();
        host = "http://127.0.0.1:" + port;
        SpringApplication springApplication = new SpringApplication(TestApplication.class);
        ctx = springApplication.run("--spring.profiles.active=client", "--server.port=" + port);
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
        final String url = "/test/hello";
        String resourceName = "GET:" + url;
        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {

            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
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
        final String url = "/test/async-hello";
        final String resourceName = "GET:" + url;

        Future<Response> future = SentinelJaxRsClientTemplate.executeAsync(resourceName, new Supplier<Future<Response>>() {
            @Override
            public Future<Response> get() {
                return client.target(host).path(url).request()
                        .async()
                        .get();
            }
        });

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(HELLO_STR, future.get().readEntity(String.class));

        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testCustomResourceName() {
        final String url = "/test/hello/{name}";
        final String resourceName = "GET:" + url;

        Response response1 = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host)
                        .path(url)
                        .resolveTemplate("name", "abc")
                        .request()
                        .get();
            }
        });
        assertEquals(200, response1.getStatus());
        assertEquals("Hello abc !", response1.readEntity(String.class));

        Response response2 = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host)
                        .path(url)
                        .resolveTemplate("name", "def")
                        .request()
                        .get();
            }
        });
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
        final String url = "/test/hello";
        final String resourceName = "GET:" + url;
        configureRulesFor(resourceName, 0);

        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
        assertEquals(javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS.getStatusCode(), response.getStatus());
        assertEquals("Blocked by Sentinel (flow limiting)", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testClientCustomFallback() {
        final String url = "/test/hello";
        final String resourceName = "GET:" + url;
        configureRulesFor(resourceName, 0);

        SentinelJaxRsConfig.setJaxRsFallback(new SentinelJaxRsFallback() {
            @Override
            public javax.ws.rs.core.Response fallbackResponse(String route, Throwable cause) {
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.OK)
                        .entity("Blocked by Sentinel (flow limiting)")
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }

            @Override
            public Future<Response> fallbackFutureResponse(final String route, final Throwable cause) {
                return new FutureTask<>(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        return fallbackResponse(route, cause);
                    }
                });
            }
        });

        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
        assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Blocked by Sentinel (flow limiting)", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testServerReturn400() {
        final String url = "/test/400";
        final String resourceName = "GET:" + url;
        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("test return 400", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testServerReturn500() {
        final String url = "/test/ex";
        final String resourceName = "GET:" + url;
        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("test exception mapper", response.readEntity(String.class));


        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testServerTimeout() {
        final String url = "/test/delay/10";
        final String resourceName = "GET:/test/delay/{seconds}";
        try {
            SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
                @Override
                public Response get() {
                    return client.target(host).path(url).request()
                            .get();
                }
            });
        } catch (ProcessingException e) {
            //ignore
        }

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testFutureGetServerTimeout() {
        final String url = "/test/delay/10";
        final String resourceName = "GET:/test/delay/{seconds}";
        try {
            Future<Response> future = SentinelJaxRsClientTemplate.executeAsync(resourceName, new Supplier<Future<Response>>() {
                @Override
                public Future<Response> get() {
                    return client.target(host).path(url).request()
                            .async()
                            .get();
                }
            });
            future.get();
        } catch (Exception e) {
            //ignore
        }

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testFutureGetTimeout() {
        final String url = "/test/delay/10";
        final String resourceName = "GET:/test/delay/{seconds}";
        try {
            Future<Response> future = SentinelJaxRsClientTemplate.executeAsync(resourceName, new Supplier<Future<Response>>() {
                @Override
                public Future<Response> get() {
                    return client.target(host).path(url).request()
                            .async()
                            .get();
                }
            });
            future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            //ignore
        }

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
    }

    @Test
    public void testCancelFuture() {
        final String url = "/test/delay/10";
        final String resourceName = "GET:/test/delay/{seconds}";
        try {
            Future<Response> future = SentinelJaxRsClientTemplate.executeAsync(resourceName, new Supplier<Future<Response>>() {
                @Override
                public Future<Response> get() {
                    return client.target(host).path(url).request()
                            .async()
                            .get();
                }
            });
            future.cancel(false);
        } catch (Exception e) {
            //ignore
        }

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
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
