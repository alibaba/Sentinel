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
package com.alibaba.csp.sentinel.command.handler;

import java.util.List;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that {@link ModifyRulesCommandHandler} treats the {@code data} parameter as plain
 * JSON: the transports URL-decode request parameters before they reach a {@link CommandRequest},
 * so the handler must not decode the value a second time.
 */
public class ModifyRulesCommandHandlerDecodeTest {

    private final ModifyRulesCommandHandler handler = new ModifyRulesCommandHandler();

    private List<FlowRule> previousFlowRules;
    private List<AuthorityRule> previousAuthorityRules;
    private WritableDataSource<List<FlowRule>> previousFlowDataSource;
    private WritableDataSource<List<AuthorityRule>> previousAuthorityDataSource;

    private RecordingFlowDataSource flowDataSource;
    private RecordingAuthorityDataSource authorityDataSource;

    @Before
    public void setUp() {
        previousFlowRules = FlowRuleManager.getRules();
        previousAuthorityRules = AuthorityRuleManager.getRules();
        previousFlowDataSource = WritableDataSourceRegistry.getFlowDataSource();
        previousAuthorityDataSource = WritableDataSourceRegistry.getAuthorityDataSource();

        flowDataSource = new RecordingFlowDataSource();
        authorityDataSource = new RecordingAuthorityDataSource();
        WritableDataSourceRegistry.registerFlowDataSource(flowDataSource);
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityDataSource);
    }

    @After
    public void tearDown() {
        try {
            FlowRuleManager.loadRules(previousFlowRules);
            AuthorityRuleManager.loadRules(previousAuthorityRules);
        } finally {
            WritableDataSourceRegistry.registerFlowDataSource(previousFlowDataSource);
            WritableDataSourceRegistry.registerAuthorityDataSource(previousAuthorityDataSource);
        }
    }

    @Test
    public void testFlowOrdinaryAsciiResourceIsKept() {
        assertFlowRuleApplied("decodeAsciiResource");
    }

    @Test
    public void testFlowUnicodeResourceIsKept() {
        assertFlowRuleApplied("限流资源api");
    }

    @Test
    public void testFlowRealSpaceResourceIsKept() {
        assertFlowRuleApplied("decode space resource");
    }

    @Test
    public void testFlowGenuinePlusResourceIsKept() {
        assertFlowRuleApplied("decode+plus");
    }

    @Test
    public void testFlowLiteralPercent2BResourceIsKept() {
        assertFlowRuleApplied("decode%2Bpercent");
    }

    @Test
    public void testFlowLonePercentResourceIsAccepted() {
        assertFlowRuleApplied("decode50%off");
    }

    @Test
    public void testAuthorityOrdinaryAsciiResourceIsKept() {
        assertAuthorityRuleApplied("authorityAsciiResource");
    }

    @Test
    public void testAuthorityGenuinePlusResourceIsKept() {
        assertAuthorityRuleApplied("authority+plus");
    }

    @Test
    public void testAuthorityLiteralPercent2BResourceIsKept() {
        assertAuthorityRuleApplied("authority%2Bpercent");
    }

    /**
     * Sends the {@code data} parameter exactly as the transports hand it over
     * (already URL-decoded) and checks the rule manager and the writable data source.
     */
    private void assertFlowRuleApplied(String resource) {
        String data = "[{\"resource\":\"" + resource + "\",\"limitApp\":\"default\",\"grade\":1,"
            + "\"count\":10.0,\"strategy\":0,\"controlBehavior\":0}]";

        CommandResponse<String> response = handler.handle(setRulesRequest("flow", data));
        assertEquals("success", response.getResult());

        List<FlowRule> rules = FlowRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(resource, rules.get(0).getResource());

        List<FlowRule> written = flowDataSource.getLastValue();
        assertEquals(1, written.size());
        assertEquals(resource, written.get(0).getResource());
    }

    private void assertAuthorityRuleApplied(String resource) {
        String data = "[{\"resource\":\"" + resource + "\",\"limitApp\":\"appA\",\"strategy\":0}]";

        CommandResponse<String> response = handler.handle(setRulesRequest("authority", data));
        assertEquals("success", response.getResult());

        List<AuthorityRule> rules = AuthorityRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(resource, rules.get(0).getResource());

        List<AuthorityRule> written = authorityDataSource.getLastValue();
        assertEquals(1, written.size());
        assertEquals(resource, written.get(0).getResource());
    }

    private CommandRequest setRulesRequest(String type, String decodedData) {
        CommandRequest request = new CommandRequest();
        request.addParam("type", type);
        request.addParam("data", decodedData);
        return request;
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
