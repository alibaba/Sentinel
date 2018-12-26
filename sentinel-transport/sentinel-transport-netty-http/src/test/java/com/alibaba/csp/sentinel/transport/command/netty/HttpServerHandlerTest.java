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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import com.alibaba.csp.sentinel.transport.command.NettyHttpCommandCenter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link HttpServerHandler}.
 *
 * @author cdfive
 * @date 2018-12-17
 */
public class HttpServerHandlerTest {

    private static String CRLF = "\r\n";

    private static String SENTINEL_CHARSET_NAME = SentinelConfig.charset();

    private static Charset SENTINEL_CHARSET = Charset.forName(SENTINEL_CHARSET_NAME);

    private static EmbeddedChannel embeddedChannel;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // don't execute InitExecutor.doInit, to avoid CommandCenter SPI loaded
        Field[] declaredFields = InitExecutor.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.getName().equals("initialized")) {
                declaredField.setAccessible(true);
                ((AtomicBoolean)declaredField.get(InitExecutor.class)).set(true);
            }
        }

        // create NettyHttpCommandCenter to create HttpServer
        CommandCenter commandCenter = new NettyHttpCommandCenter();
        // call beforeStart to register handlers
        commandCenter.beforeStart();
    }

    @Before
    public void before() {
        // the same Handlers in order as the ChannelPipeline in HttpServerInitializer
        HttpRequestDecoder httpRequestDecoder = new HttpRequestDecoder();
        HttpObjectAggregator httpObjectAggregator = new HttpObjectAggregator(1024 * 1024);
        HttpResponseEncoder httpResponseEncoder = new HttpResponseEncoder();

        HttpServerHandler httpServerHandler = new HttpServerHandler();

        // create new EmbeddedChannel every method call
        embeddedChannel = new EmbeddedChannel(httpRequestDecoder, httpObjectAggregator, httpResponseEncoder, httpServerHandler);
    }

    @Test
    public void testInvalidCommand() {
        String httpRequestStr = "GET / HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = "Invalid command";

        processError(httpRequestStr, body);
    }

    @Test
    public void testUnknownCommand() {
        String httpRequestStr = "GET /aaa HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = String.format("Unknown command \"%s\"", "aaa");

        processError(httpRequestStr, body);
    }

    @Test
    public void testVersionCommand() {
        String httpRequestStr = "GET /version HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = Constants.SENTINEL_VERSION;

        processSuccess(httpRequestStr, body);
    }

    @Test
    public void testGetRuleCommandInvalidType() {
        String httpRequestStr = "GET /getRules HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = "invalid type";

        processFailed(httpRequestStr, body);
    }

    @Test
    public void testGetRuleCommandFlowEmptyRule() {
        String httpRequestStr = "GET /getRules?type=flow HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = "[]";

        processSuccess(httpRequestStr, body);
    }

//    FIXME byteBuf.toString can't get body response now, need to find another way
//    @Test
    public void testGetRuleCommandFlowSomeRules() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource("key");
        rule1.setCount(20);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);

        String httpRequestStr = "GET /getRules?type=flow HTTP/1.1" + CRLF
                              + "Host: localhost:8719" + CRLF
                              + CRLF;
        String body = "";

        processSuccess(httpRequestStr, body);
    }

    private void processError(String httpRequestStr, String body) {
        processError(httpRequestStr, BAD_REQUEST, body);
    }

    private void processError(String httpRequestStr, HttpResponseStatus status, String body) {
        String httpResponseStr = processResponse(httpRequestStr);
        assertErrorStatusAndBody(status, body, httpResponseStr);
    }

    private void processSuccess(String httpRequestStr, String body) {
        process(httpRequestStr, OK, body);
    }

    private void processFailed(String httpRequestStr, String body) {
        process(httpRequestStr, BAD_REQUEST, body);
    }

    private void process(String httpRequestStr, HttpResponseStatus status, String body) {
        String responseStr = processResponse(httpRequestStr);
        assertStatusAndBody(status, body, responseStr);
    }

    private String processResponse(String httpRequestStr) {
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer(httpRequestStr.getBytes(SENTINEL_CHARSET)));

        ByteBuf byteBuf = embeddedChannel.readOutbound();

        String responseStr = byteBuf.toString(SENTINEL_CHARSET);
        return responseStr;
    }

    private void assertErrorStatusAndBody(HttpResponseStatus status, String body, String httpResponseStr) {
        StringBuilder text = new StringBuilder();
        text.append(HttpVersion.HTTP_1_1.toString()).append(' ').append(status.toString()).append(CRLF);
        text.append("Content-Type: text/plain; charset=").append(SENTINEL_CHARSET_NAME).append(CRLF);
        text.append(CRLF);
        text.append(body);

        assertEquals(text.toString(), httpResponseStr);
    }

    private void assertStatusAndBody(HttpResponseStatus status, String body, String httpResponseStr) {
        StringBuilder text = new StringBuilder();
        text.append(HttpVersion.HTTP_1_1.toString()).append(' ').append(status.toString()).append(CRLF);
        text.append("Content-Type: text/plain; charset=").append(SENTINEL_CHARSET_NAME).append(CRLF);
        text.append("content-length: " + body.length()).append(CRLF);
        text.append("connection: close").append(CRLF);
        text.append(CRLF);
        text.append(body);

        assertEquals(text.toString(), httpResponseStr);
    }
}