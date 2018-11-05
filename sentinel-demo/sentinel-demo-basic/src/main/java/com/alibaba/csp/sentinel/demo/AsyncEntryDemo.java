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
package com.alibaba.csp.sentinel.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * An example for asynchronous entry in Sentinel.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class AsyncEntryDemo {

    private void invoke(String arg, Consumer<String> handler) {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                String resp = arg + ": " + System.currentTimeMillis();
                handler.accept(resp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void anotherAsync() {
        try {
            final AsyncEntry entry = SphU.asyncEntry("test-another-async");

            CompletableFuture.runAsync(() -> {
                ContextUtil.runOnContext(entry.getAsyncContext(), () -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        // Normal entry nested in asynchronous entry.
                        anotherSyncInAsync();

                        System.out.println("Async result: 666");
                    } catch (InterruptedException e) {
                        // Ignore.
                    } finally {
                        entry.exit();
                    }
                });
            });
        } catch (BlockException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchSync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-sync");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void fetchSyncInAsync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-sync-in-async");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void anotherSyncInAsync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-another-sync-in-async");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void directlyAsync() {
        try {
            final AsyncEntry entry = SphU.asyncEntry("test-async-not-nested");

            this.invoke("abc", result -> {
                // If no nested entry later, we don't have to wrap in `ContextUtil.runOnContext()`.
                try {
                    // Here to handle the async result (without other entry).
                } finally {
                    // Exit the async entry.
                    entry.exit();
                }
            });
        } catch (BlockException e) {
            // Request blocked, handle the exception.
            e.printStackTrace();
        }
    }

    private void doAsyncThenSync() {
        try {
            // First we call an asynchronous resource.
            final AsyncEntry entry = SphU.asyncEntry("test-async");
            this.invoke("abc", resp -> {
                // The thread is different from original caller thread for async entry.
                // So we need to wrap in the async context so that nested invocation entry
                // can be linked to the parent asynchronous entry.
                ContextUtil.runOnContext(entry.getAsyncContext(), () -> {
                    try {
                        // In the callback, we do another async invocation several times under the async context.
                        for (int i = 0; i < 7; i++) {
                            anotherAsync();
                        }

                        System.out.println(resp);

                        // Then we do a sync (normal) entry under current async context.
                        fetchSyncInAsync();
                    } finally {
                        // Exit the async entry.
                        entry.exit();
                    }
                });
            });
            // Then we call a sync resource.
            fetchSync();
        } catch (BlockException ex) {
            // Request blocked, handle the exception.
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        initFlowRule();

        AsyncEntryDemo service = new AsyncEntryDemo();

        // Expected invocation chain:
        //
        // EntranceNode: machine-root
        // -EntranceNode: async-context
        // --test-top
        // ---test-sync
        // ---test-async
        // ----test-another-async
        // -----test-another-sync-in-async
        // ----test-sync-in-async
        ContextUtil.enter("async-context", "originA");
        Entry entry = null;
        try {
            entry = SphU.entry("test-top");
            System.out.println("Do something...");
            service.doAsyncThenSync();
        } catch (BlockException ex) {
            // Request blocked, handle the exception.
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }

        TimeUnit.SECONDS.sleep(20);
    }

    private static void initFlowRule() {
        // Rule 1 won't take effect as the limitApp doesn't match.
        FlowRule rule1 = new FlowRule()
            .setResource("test-another-sync-in-async")
            .setLimitApp("originB")
            .as(FlowRule.class)
            .setCount(4)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Rule 2 will take effect.
        FlowRule rule2 = new FlowRule()
            .setResource("test-another-async")
            .setLimitApp("default")
            .as(FlowRule.class)
            .setCount(5)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        List<FlowRule> ruleList = Arrays.asList(rule1, rule2);
        FlowRuleManager.loadRules(ruleList);
    }
}
