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
package com.alibaba.csp.sentinel;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration test for asynchronous entry, including common scenarios.
 *
 * @author Eric Zhao
 */
public class AsyncEntryIntegrationTest {

    @Before
    public void clearContext() {
        ContextTestUtil.cleanUpContext();
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private void anotherAsync() {
        try {
            final AsyncEntry entry = SphU.asyncEntry("test-another-async");

            runAsync(new Runnable() {
                @Override
                public void run() {
                    ContextUtil.runOnContext(entry.getAsyncContext(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TimeUnit.SECONDS.sleep(2);
                                anotherSyncInAsync();
                                System.out.println("Async result: 666");
                            } catch (InterruptedException e) {
                                // Ignore.
                            } finally {
                                entry.exit();
                            }
                        }
                    });
                }
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

    public void anotherSyncInAsync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-another-in-async");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void doAsyncThenSync() {
        try {
            // First we call an asynchronous resource.
            final AsyncEntry entry = SphU.asyncEntry("test-async");
            this.invoke("abc", new Consumer<String>() {
                @Override
                public void accept(final String resp) {
                    // The thread is different from original caller thread for async entry.
                    // So we need to wrap in the async context so that nested sync invocation entry
                    // can be linked to the parent asynchronous entry.
                    ContextUtil.runOnContext(entry.getAsyncContext(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // In the callback, we do another async invocation under the async context.
                                anotherAsync();

                                System.out.println(resp);

                                // Then we do a sync entry under current async context.
                                fetchSyncInAsync();
                            } finally {
                                // Exit the async entry.
                                entry.exit();
                            }
                        }
                    });
                }
            });
            // Then we call a sync resource.
            fetchSync();
        } catch (BlockException ex) {
            // Request blocked, handle the exception.
            ex.printStackTrace();
        }
    }

    @Test
    public void testAsyncEntryUnderSyncEntry() throws Exception {
        // Expected invocation chain:
        // EntranceNode: machine-root
        // -EntranceNode: async-context
        // --test-top
        // ---test-async
        // ----test-sync-in-async
        // ----test-another-async
        // -----test-another-in-async
        // ---test-sync
        ContextUtil.enter(contextName, origin);
        Entry entry = null;
        try {
            entry = SphU.entry("test-top");
            doAsyncThenSync();
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }

        TimeUnit.SECONDS.sleep(15);

        testInvocationTreeCorrect();
    }

    private void testInvocationTreeCorrect() {
        DefaultNode root = Constants.ROOT;
        DefaultNode entranceNode = shouldHasChildFor(root, contextName);
        DefaultNode testTopNode = shouldHasChildFor(entranceNode, "test-top");
        DefaultNode testAsyncNode = shouldHasChildFor(testTopNode, "test-async");
        shouldHasChildFor(testTopNode, "test-sync");
        shouldHasChildFor(testAsyncNode, "test-sync-in-async");
        DefaultNode anotherAsyncInAsyncNode = shouldHasChildFor(testAsyncNode, "test-another-async");
        shouldHasChildFor(anotherAsyncInAsyncNode, "test-another-in-async");
    }

    private DefaultNode shouldHasChildFor(DefaultNode root, String resourceName) {
        Set<Node> nodeSet = root.getChildList();
        if (nodeSet == null || nodeSet.isEmpty()) {
            fail("Child nodes should not be empty: " + root.getId().getName());
        }
        for (Node node : nodeSet) {
            if (node instanceof DefaultNode) {
                DefaultNode dn = (DefaultNode)node;
                if (dn.getId().getName().equals(resourceName)) {
                    return dn;
                }
            }
        }
        fail(String.format("The given node <%s> does not have child for resource <%s>",
            root.getId().getName(), resourceName));
        return null;
    }

    @After
    public void shutdown() {
        pool.shutdownNow();
        ContextTestUtil.cleanUpContext();
    }

    private void runAsync(Runnable f) {
        // In Java 8, we can use CompletableFuture.runAsync(f) instead.
        pool.submit(f);
    }

    private void invoke(final String arg, final Consumer<String> handler) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    String resp = arg + ": " + System.currentTimeMillis();
                    handler.accept(resp);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private interface Consumer<T> {
        void accept(T t);
    }

    private final String contextName = "async-context";
    private final String origin = "originA";
}
