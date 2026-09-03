/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.ai.mcp;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.spring.ai.mcp.SentinelMcpToolSpecificationDecorator.Invocation;
import com.alibaba.csp.sentinel.adapter.spring.ai.mcp.SentinelMcpToolSpecificationDecorator.InvocationFactory;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SentinelMcpToolSpecificationDecoratorTest {

    private static final CallToolResult RESULT = CallToolResult.builder()
        .content(Collections.emptyList()).isError(false).build();
    private static final CallToolRequest REQUEST = CallToolRequest.builder("echo")
        .arguments(Collections.emptyMap()).build();

    private Tool tool;

    @Before
    public void setUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.getClusterNodeMap().clear();
        tool = mock(Tool.class);
        when(tool.name()).thenReturn("echo");
    }

    @After
    public void tearDown() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testSyncSuccess() {
        RecordingFactory factory = new RecordingFactory();
        SyncToolSpecification original = syncTool((exchange, arguments) -> RESULT);

        CallToolResult actual = SentinelMcpToolSpecificationDecorator.decorate(original, factory)
            .callHandler().apply(null, REQUEST);

        assertSame(RESULT, actual);
        assertEquals("mcp:tool:echo", factory.resourceName);
        assertEquals(1, factory.invocation.exitCount.get());
        assertEquals(0, factory.invocation.traceCount.get());
    }

    @Test
    public void testSyncExceptionIsTracedAndEntryExits() {
        RecordingFactory factory = new RecordingFactory();
        IllegalStateException failure = new IllegalStateException("boom");
        SyncToolSpecification original = syncTool((exchange, arguments) -> {
            throw failure;
        });

        IllegalStateException actual = expectThrows(IllegalStateException.class, () ->
            SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST));

        assertSame(failure, actual);
        assertSame(failure, factory.invocation.lastFailure);
        assertEquals(1, factory.invocation.traceCount.get());
        assertEquals(1, factory.invocation.exitCount.get());
    }

    @Test
    public void testSyncBlockSkipsToolCall() {
        RecordingFactory factory = new RecordingFactory();
        factory.blocked = true;
        AtomicInteger calls = new AtomicInteger();
        SyncToolSpecification original = syncTool((exchange, arguments) -> {
            calls.incrementAndGet();
            return RESULT;
        });

        RuntimeException actual = expectThrows(RuntimeException.class, () ->
            SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST));

        assertTrue(BlockException.isBlockException(actual));
        assertEquals(0, calls.get());
        assertEquals(0, factory.invocation.exitCount.get());
    }

    @Test
    public void testRealSentinelRuleBlocksSyncTool() {
        String resourceName = "mcp:tool:echo";
        FlowRule rule = new FlowRule().setCount(0).setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setResource(resourceName).as(FlowRule.class);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
        SyncToolSpecification original = syncTool((exchange, arguments) -> RESULT);

        RuntimeException actual = expectThrows(RuntimeException.class, () ->
            SentinelMcpToolSpecificationDecorator.decorate(original).callHandler().apply(null, REQUEST));

        assertTrue(BlockException.isBlockException(actual));
        ClusterNode node = ClusterBuilderSlot.getClusterNode(resourceName, EntryType.IN);
        assertEquals(1, node.blockRequest());
    }

    @Test
    public void testAsyncCompletionExitsOnce() {
        RecordingFactory factory = new RecordingFactory();
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> Mono.just(RESULT));

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .expectNext(RESULT)
            .verifyComplete();

        assertEquals(1, factory.invocation.exitCount.get());
        assertEquals(0, factory.invocation.traceCount.get());
    }

    @Test
    public void testAsyncErrorIsTracedAndEntryExits() {
        RecordingFactory factory = new RecordingFactory();
        IllegalStateException failure = new IllegalStateException("boom");
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> Mono.error(failure));

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertSame(failure, factory.invocation.lastFailure);
        assertEquals(1, factory.invocation.traceCount.get());
        assertEquals(1, factory.invocation.exitCount.get());
    }

    @Test
    public void testAsyncCancellationExitsOnce() {
        RecordingFactory factory = new RecordingFactory();
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> Mono.never());

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .thenCancel()
            .verify();

        assertEquals(1, factory.invocation.exitCount.get());
        assertEquals(0, factory.invocation.traceCount.get());
    }

    @Test
    public void testAsyncBlockSkipsToolCall() {
        RecordingFactory factory = new RecordingFactory();
        factory.blocked = true;
        AtomicInteger calls = new AtomicInteger();
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> {
            calls.incrementAndGet();
            return Mono.just(RESULT);
        });

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .expectError(FlowException.class)
            .verify();

        assertEquals(0, calls.get());
        assertEquals(0, factory.invocation.exitCount.get());
    }

    @Test
    public void testAsyncImmediateExceptionIsTracedAndEntryExits() {
        RecordingFactory factory = new RecordingFactory();
        IllegalStateException failure = new IllegalStateException("boom");
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> {
            throw failure;
        });

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertSame(failure, factory.invocation.lastFailure);
        assertEquals(1, factory.invocation.traceCount.get());
        assertEquals(1, factory.invocation.exitCount.get());
    }

    @Test
    public void testRealSentinelRuleBlocksAsyncTool() {
        String resourceName = "mcp:tool:echo";
        FlowRule rule = new FlowRule().setCount(0).setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setResource(resourceName).as(FlowRule.class);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
        AsyncToolSpecification original = asyncTool((exchange, arguments) -> Mono.just(RESULT));

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original)
                .callHandler().apply(null, REQUEST))
            .expectErrorMatches(BlockException::isBlockException)
            .verify();

    }

    @Test
    public void testStatelessSyncToolIsProtected() {
        RecordingFactory factory = new RecordingFactory();
        McpStatelessServerFeatures.SyncToolSpecification original =
            McpStatelessServerFeatures.SyncToolSpecification.builder().tool(tool)
                .callHandler((context, arguments) -> RESULT).build();

        CallToolResult actual = SentinelMcpToolSpecificationDecorator.decorate(original, factory)
            .callHandler().apply(null, REQUEST);

        assertSame(RESULT, actual);
        assertEquals("mcp:tool:echo", factory.resourceName);
        assertEquals(1, factory.invocation.exitCount.get());
    }

    @Test
    public void testStatelessAsyncToolIsProtected() {
        RecordingFactory factory = new RecordingFactory();
        McpStatelessServerFeatures.AsyncToolSpecification original =
            McpStatelessServerFeatures.AsyncToolSpecification.builder().tool(tool)
                .callHandler((context, arguments) -> Mono.just(RESULT)).build();

        StepVerifier.create(SentinelMcpToolSpecificationDecorator.decorate(original, factory)
                .callHandler().apply(null, REQUEST))
            .expectNext(RESULT)
            .verifyComplete();

        assertEquals("mcp:tool:echo", factory.resourceName);
        assertEquals(1, factory.invocation.exitCount.get());
    }

    private static <T extends Throwable> T expectThrows(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            assertTrue("Expected " + type.getName() + " but got " + throwable.getClass().getName(),
                type.isInstance(throwable));
            return type.cast(throwable);
        }
        throw new AssertionError("Expected " + type.getName() + " to be thrown");
    }

    private SyncToolSpecification syncTool(
        java.util.function.BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange,
            CallToolRequest, CallToolResult> handler) {
        return SyncToolSpecification.builder().tool(tool).callHandler(handler).build();
    }

    private AsyncToolSpecification asyncTool(
        java.util.function.BiFunction<io.modelcontextprotocol.server.McpAsyncServerExchange,
            CallToolRequest, Mono<CallToolResult>> handler) {
        return AsyncToolSpecification.builder().tool(tool).callHandler(handler).build();
    }

    private static final class RecordingFactory implements InvocationFactory {

        private final RecordingInvocation invocation = new RecordingInvocation();
        private String resourceName;
        private boolean blocked;

        @Override
        public Invocation enter(String resourceName, boolean async) throws FlowException {
            this.resourceName = resourceName;
            if (blocked) {
                throw new FlowException(resourceName);
            }
            return invocation;
        }
    }

    private static final class RecordingInvocation implements Invocation {

        private final AtomicInteger traceCount = new AtomicInteger();
        private final AtomicInteger exitCount = new AtomicInteger();
        private Throwable lastFailure;

        @Override
        public void trace(Throwable throwable) {
            lastFailure = throwable;
            traceCount.incrementAndGet();
        }

        @Override
        public void exit() {
            exitCount.incrementAndGet();
        }
    }
}
