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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Mono;

/**
 * Decorates Spring AI MCP tool specifications with tool-level Sentinel protection.
 *
 * @author chengliyao
 */
public final class SentinelMcpToolSpecificationDecorator {

    public static final String DEFAULT_RESOURCE_PREFIX = "mcp:tool:";

    private static final InvocationFactory DEFAULT_INVOCATION_FACTORY = new SentinelInvocationFactory();

    /**
     * Decorates a synchronous MCP tool specification.
     *
     * @param specification original tool specification
     * @return protected tool specification
     */
    public static SyncToolSpecification decorate(SyncToolSpecification specification) {
        return decorate(specification, DEFAULT_INVOCATION_FACTORY);
    }

    /**
     * Decorates an asynchronous MCP tool specification.
     *
     * @param specification original tool specification
     * @return protected tool specification
     */
    public static AsyncToolSpecification decorate(AsyncToolSpecification specification) {
        return decorate(specification, DEFAULT_INVOCATION_FACTORY);
    }

    /**
     * Decorates a synchronous stateless MCP tool specification.
     *
     * @param specification original tool specification
     * @return protected tool specification
     */
    public static McpStatelessServerFeatures.SyncToolSpecification decorate(
        McpStatelessServerFeatures.SyncToolSpecification specification) {
        return decorate(specification, DEFAULT_INVOCATION_FACTORY);
    }

    /**
     * Decorates an asynchronous stateless MCP tool specification.
     *
     * @param specification original tool specification
     * @return protected tool specification
     */
    public static McpStatelessServerFeatures.AsyncToolSpecification decorate(
        McpStatelessServerFeatures.AsyncToolSpecification specification) {
        return decorate(specification, DEFAULT_INVOCATION_FACTORY);
    }

    static SyncToolSpecification decorate(SyncToolSpecification specification, InvocationFactory factory) {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(factory, "factory");
        String resourceName = resourceName(specification.tool().name());
        return SyncToolSpecification.builder().tool(specification.tool())
            .callHandler((exchange, arguments) -> protectSync(resourceName, factory,
                () -> specification.callHandler().apply(exchange, arguments)))
            .build();
    }

    static AsyncToolSpecification decorate(AsyncToolSpecification specification, InvocationFactory factory) {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(factory, "factory");
        String resourceName = resourceName(specification.tool().name());
        return AsyncToolSpecification.builder().tool(specification.tool())
            .callHandler((exchange, arguments) -> protectAsync(resourceName, factory,
                () -> specification.callHandler().apply(exchange, arguments)))
            .build();
    }

    static McpStatelessServerFeatures.SyncToolSpecification decorate(
        McpStatelessServerFeatures.SyncToolSpecification specification, InvocationFactory factory) {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(factory, "factory");
        String resourceName = resourceName(specification.tool().name());
        return McpStatelessServerFeatures.SyncToolSpecification.builder().tool(specification.tool())
            .callHandler((context, arguments) -> protectSync(resourceName, factory,
                () -> specification.callHandler().apply(context, arguments)))
            .build();
    }

    static McpStatelessServerFeatures.AsyncToolSpecification decorate(
        McpStatelessServerFeatures.AsyncToolSpecification specification, InvocationFactory factory) {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(factory, "factory");
        String resourceName = resourceName(specification.tool().name());
        return McpStatelessServerFeatures.AsyncToolSpecification.builder().tool(specification.tool())
            .callHandler((context, arguments) -> protectAsync(resourceName, factory,
                () -> specification.callHandler().apply(context, arguments)))
            .build();
    }

    private static CallToolResult protectSync(String resourceName, InvocationFactory factory,
                                              Supplier<CallToolResult> toolCall) {
        Invocation invocation;
        try {
            invocation = factory.enter(resourceName, false);
        } catch (BlockException ex) {
            throw ex.toRuntimeException();
        }
        try {
            return toolCall.get();
        } catch (Throwable throwable) {
            invocation.trace(throwable);
            throw throwable;
        } finally {
            invocation.exit();
        }
    }

    private static Mono<CallToolResult> protectAsync(String resourceName, InvocationFactory factory,
                                                     Supplier<Mono<CallToolResult>> toolCall) {
        return Mono.defer(() -> {
            Invocation invocation;
            try {
                invocation = factory.enter(resourceName, true);
            } catch (BlockException ex) {
                return Mono.error(ex);
            }

            try {
                Mono<CallToolResult> result = Objects.requireNonNull(toolCall.get(),
                    "MCP async tool returned null");
                return result.doOnError(invocation::trace).doFinally(signalType -> invocation.exit());
            } catch (Throwable throwable) {
                invocation.trace(throwable);
                invocation.exit();
                return Mono.error(throwable);
            }
        });
    }

    private static String resourceName(String toolName) {
        return DEFAULT_RESOURCE_PREFIX + Objects.requireNonNull(toolName, "toolName");
    }

    interface InvocationFactory {
        Invocation enter(String resourceName, boolean async) throws BlockException;
    }

    interface Invocation {
        void trace(Throwable throwable);

        void exit();
    }

    private static final class SentinelInvocationFactory implements InvocationFactory {

        @Override
        public Invocation enter(String resourceName, boolean async) throws BlockException {
            if (async) {
                AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
                return new AsyncSentinelInvocation(entry);
            }
            Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
            return new SyncSentinelInvocation(entry);
        }
    }

    private abstract static class AbstractSentinelInvocation implements Invocation {

        private final AtomicBoolean exited = new AtomicBoolean();

        @Override
        public final void exit() {
            if (exited.compareAndSet(false, true)) {
                doExit();
            }
        }

        abstract void doExit();
    }

    private static final class SyncSentinelInvocation extends AbstractSentinelInvocation {

        private final Entry entry;

        private SyncSentinelInvocation(Entry entry) {
            this.entry = entry;
        }

        @Override
        public void trace(Throwable throwable) {
            Tracer.traceEntry(throwable, entry);
        }

        @Override
        void doExit() {
            entry.exit();
        }
    }

    private static final class AsyncSentinelInvocation extends AbstractSentinelInvocation {

        private final AsyncEntry entry;

        private AsyncSentinelInvocation(AsyncEntry entry) {
            this.entry = entry;
        }

        @Override
        public void trace(Throwable throwable) {
            Tracer.traceContext(throwable, entry.getAsyncContext());
        }

        @Override
        void doExit() {
            entry.exit();
        }
    }

    private SentinelMcpToolSpecificationDecorator() {}
}
