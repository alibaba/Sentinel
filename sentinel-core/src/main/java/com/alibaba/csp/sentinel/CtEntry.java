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

import java.util.LinkedList;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.function.BiConsumer;

/**
 * Linked entry within current context.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
class CtEntry extends Entry {

    protected Entry parent = null;
    protected Entry child = null;

    protected ProcessorSlot<Object> chain;
    protected Context context;
    protected LinkedList<BiConsumer<Context, Entry>> exitHandlers;

    CtEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        this(resourceWrapper, chain, context, 1, OBJECTS0);
    }

    CtEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context, int count, Object[] args) {
        super(resourceWrapper, count, args);
        this.chain = chain;
        this.context = context;

        setUpEntryFor(context);
    }

    private void setUpEntryFor(Context context) {
        // The entry should not be associated to NullContext.
        if (context instanceof NullContext) {
            return;
        }
        this.parent = context.getCurEntry();
        if (parent != null) {
            ((CtEntry) parent).child = this;
        }
        context.setCurEntry(this);
    }

    @Override
    public void exit(int count, Object... args) throws ErrorEntryFreeException {
        trueExit(count, args);
    }

    /**
     * Note: the exit handlers will be called AFTER onExit of slot chain.
     */
    private void callExitHandlersAndCleanUp(Context ctx) {
        if (exitHandlers != null && !exitHandlers.isEmpty()) {
            for (BiConsumer<Context, Entry> handler : this.exitHandlers) {
                try {
                    handler.accept(ctx, this);
                } catch (Exception e) {
                    RecordLog.warn("Error occurred when invoking entry exit handler, current entry: "
                        + resourceWrapper.getName(), e);
                }
            }
            exitHandlers = null;
        }
    }

    protected void exitForContext(Context context, int count, Object... args) throws ErrorEntryFreeException {
        if (context != null) {
            // Null context should exit without clean-up.
            if (context instanceof NullContext) {
                return;
            }

            if (context.getCurEntry() != this) {
                String curEntryNameInContext = context.getCurEntry() == null ? null
                    : context.getCurEntry().getResourceWrapper().getName();
                // Clean previous call stack.
                CtEntry e = (CtEntry) context.getCurEntry();
                while (e != null) {
                    e.exit(count, args);
                    e = (CtEntry) e.parent;
                }
                String errorMessage = String.format("The order of entry exit can't be paired with the order of entry"
                        + ", current entry in context: <%s>, but expected: <%s>", curEntryNameInContext,
                    resourceWrapper.getName());
                throw new ErrorEntryFreeException(errorMessage);
            } else {
                // Go through the onExit hook of all slots.
                if (chain != null) {
                    chain.exit(context, resourceWrapper, count, args);
                }
                // Go through the existing terminate handlers (associated to this invocation).
                callExitHandlersAndCleanUp(context);

                // Restore the call stack.
                context.setCurEntry(parent);
                if (parent != null) {
                    ((CtEntry) parent).child = null;
                }
                if (parent == null) {
                    // Default context (auto entered) will be exited automatically.
                    if (ContextUtil.isDefaultContext(context)) {
                        ContextUtil.exit();
                    }
                }
                // Clean the reference of context in current entry to avoid duplicate exit.
                clearEntryContext();
            }
        }
    }

    protected void clearEntryContext() {
        this.context = null;
    }

    @Override
    public void whenTerminate(BiConsumer<Context, Entry> handler) {
        if (this.exitHandlers == null) {
            this.exitHandlers = new LinkedList<>();
        }
        this.exitHandlers.add(handler);
    }

    @Override
    protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
        exitForContext(context, count, args);

        return parent;
    }

    @Override
    public Node getLastNode() {
        return parent == null ? null : parent.getCurNode();
    }
}