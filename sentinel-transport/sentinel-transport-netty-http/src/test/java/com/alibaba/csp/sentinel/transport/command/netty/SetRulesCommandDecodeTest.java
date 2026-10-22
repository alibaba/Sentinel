/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.command.netty;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that the {@code setRules} command keeps the {@code data} parameter intact when a
 * client URL-encodes it exactly once through the netty-http pipeline.
 */
public class SetRulesCommandDecodeTest {

    private static final String CRLF = "\r\n";
    private static final Charset SENTINEL_CHARSET = Charset.forName(SentinelConfig.charset());

    private static CommandHandler previousSetRulesHandler;
    private static boolean setRulesHandlerExisted;

    private EmbeddedChannel embeddedChannel;

    private List<FlowRule> previousFlowRules;
    private List<AuthorityRule> previousAuthorityRules;
    private WritableDataSource<List<FlowRule>> previousFlowDataSource;
    private WritableDataSource<List<AuthorityRule>> previousAuthorityDataSource;

    private RecordingFlowDataSource flowDataSource;
    private RecordingAuthorityDataSource authorityDataSource;

    @BeforeClass
    public static void beforeClass() {
        // Only the "setRules" entry is replaced; other registered commands stay untouched.
        setRulesHandlerExisted = HttpServer.handlerMap.containsKey("setRules");
        previousSetRulesHandler = HttpServer.handlerMap.get("setRules");
        HttpServer.handlerMap.put("setRules", new ModifyRulesCommandHandler());
    }

    @AfterClass
    public static void afterClass() {
        if (setRulesHandlerExisted) {
            HttpServer.handlerMap.put("setRules", previousSetRulesHandler);
        } else {
            HttpServer.handlerMap.remove("setRules");
        }
    }

    @Before
    public void setUp() {
        previousFlowRules = FlowRuleManager.getRules();
        previousAuthorityRules = AuthorityRuleManager.getRules();
        previousFlowDataSource = WritableDataSourceRegistry.getFlowDataSource();
        previousAuthorityDataSource = WritableDataSourceRegistry.getAuthorityDataSource();

        // The same handlers in order as the pipeline in HttpServerInitializer.
        embeddedChannel = new EmbeddedChannel(new HttpRequestDecoder(), new HttpObjectAggregator(1024 * 1024),
            new HttpResponseEncoder(), new HttpServerHandler());

        flowDataSource = new RecordingFlowDataSource();
        authorityDataSource = new RecordingAuthorityDataSource();
        WritableDataSourceRegistry.registerFlowDataSource(flowDataSource);
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityDataSource);
    }

    @After
    public void tearDown() {
        try {
            embeddedChannel.finishAndReleaseAll();
        } finally {
            try {
                FlowRuleManager.loadRules(previousFlowRules);
                AuthorityRuleManager.loadRules(previousAuthorityRules);
            } finally {
                WritableDataSourceRegistry.registerFlowDataSource(previousFlowDataSource);
                WritableDataSourceRegistry.registerAuthorityDataSource(previousAuthorityDataSource);
            }
        }
    }

    @Test
    public void testFlowPostOrdinaryAsciiResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("decodeAsciiResource"))), "flow", "decodeAsciiResource");
    }

    @Test
    public void testFlowPostRealSpaceResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("decode space resource"))), "flow",
            "decode space resource");
    }

    @Test
    public void testFlowPostUnicodeResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("限流资源api"))), "flow", "限流资源api");
    }

    @Test
    public void testFlowGetOrdinaryAsciiResource() throws Exception {
        assertApplied(process(getSetRules("flow", flowJson("decodeAsciiResource"))), "flow", "decodeAsciiResource");
    }

    @Test
    public void testFlowPostGenuinePlusResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("decode+plus"))), "flow", "decode+plus");
    }

    @Test
    public void testFlowPostLiteralPercent2BResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("decode%2Bpercent"))), "flow", "decode%2Bpercent");
    }

    @Test
    public void testFlowPostLonePercentResource() throws Exception {
        assertApplied(process(postSetRules("flow", flowJson("decode50%off"))), "flow", "decode50%off");
    }

    @Test
    public void testFlowGetGenuinePlusResource() throws Exception {
        assertApplied(process(getSetRules("flow", flowJson("decode+plus"))), "flow", "decode+plus");
    }

    @Test
    public void testAuthorityPostOrdinaryAsciiResource() throws Exception {
        assertApplied(process(postSetRules("authority", authorityJson("authorityAsciiResource"))), "authority",
            "authorityAsciiResource");
    }

    @Test
    public void testAuthorityPostGenuinePlusResource() throws Exception {
        assertApplied(process(postSetRules("authority", authorityJson("authority+plus"))), "authority",
            "authority+plus");
    }

    @Test
    public void testAuthorityPostLiteralPercent2BResource() throws Exception {
        assertApplied(process(postSetRules("authority", authorityJson("authority%2Bpercent"))), "authority",
            "authority%2Bpercent");
    }

    @Test
    public void testAuthorityGetGenuinePlusResource() throws Exception {
        assertApplied(process(getSetRules("authority", authorityJson("authority+plus"))), "authority",
            "authority+plus");
    }

    private String flowJson(String resource) {
        return "[{\"resource\":\"" + resource + "\",\"limitApp\":\"default\",\"grade\":1,"
            + "\"count\":10.0,\"strategy\":0,\"controlBehavior\":0}]";
    }

    private String authorityJson(String resource) {
        return "[{\"resource\":\"" + resource + "\",\"limitApp\":\"appA\",\"strategy\":0}]";
    }

    /**
     * Builds a real HTTP request whose {@code data} parameter is URL-encoded exactly once,
     * as the dashboard does ({@code UrlEncodedFormEntity}).
     */
    private String postSetRules(String type, String data) throws Exception {
        String body = "type=" + URLEncoder.encode(type, "UTF-8")
            + "&data=" + URLEncoder.encode(data, "UTF-8");
        return "POST /setRules HTTP/1.1" + CRLF
            + "Host: localhost:8719" + CRLF
            + "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" + CRLF
            + "Content-Length: " + body.getBytes(SENTINEL_CHARSET).length + CRLF
            + CRLF
            + body;
    }

    private String getSetRules(String type, String data) throws Exception {
        return "GET /setRules?type=" + URLEncoder.encode(type, "UTF-8")
            + "&data=" + URLEncoder.encode(data, "UTF-8") + " HTTP/1.1" + CRLF
            + "Host: localhost:8719" + CRLF
            + CRLF;
    }

    /**
     * Feeds the raw request through the real netty pipeline and returns the response body.
     */
    private String process(String httpRequestStr) {
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer(httpRequestStr.getBytes(SENTINEL_CHARSET)));

        StringBuilder sb = new StringBuilder();
        ByteBuf byteBuf;
        while ((byteBuf = embeddedChannel.readOutbound()) != null) {
            sb.append(byteBuf.toString(SENTINEL_CHARSET));
            byteBuf.release();
        }

        String responseStr = sb.toString();
        int bodyStart = responseStr.indexOf(CRLF + CRLF);
        return bodyStart < 0 ? responseStr : responseStr.substring(bodyStart + 4);
    }

    private void assertApplied(String responseBody, String type, String resource) {
        assertEquals("success", responseBody.trim());
        if ("flow".equals(type)) {
            List<FlowRule> rules = FlowRuleManager.getRules();
            assertEquals(1, rules.size());
            assertEquals(resource, rules.get(0).getResource());
            List<FlowRule> written = flowDataSource.getLastValue();
            assertEquals(1, written.size());
            assertEquals(resource, written.get(0).getResource());
        } else {
            List<AuthorityRule> rules = AuthorityRuleManager.getRules();
            assertEquals(1, rules.size());
            assertEquals(resource, rules.get(0).getResource());
            List<AuthorityRule> written = authorityDataSource.getLastValue();
            assertEquals(1, written.size());
            assertEquals(resource, written.get(0).getResource());
        }
    }

    private static class RecordingFlowDataSource implements WritableDataSource<List<FlowRule>> {

        private List<FlowRule> lastValue;

        @Override
        public void write(List<FlowRule> value) {
            lastValue = value;
        }

        @Override
        public void close() {
        }

        List<FlowRule> getLastValue() {
            return lastValue;
        }
    }

    private static class RecordingAuthorityDataSource implements WritableDataSource<List<AuthorityRule>> {

        private List<AuthorityRule> lastValue;

        @Override
        public void write(List<AuthorityRule> value) {
            lastValue = value;
        }

        @Override
        public void close() {
        }

        List<AuthorityRule> getLastValue() {
            return lastValue;
        }
    }
}
