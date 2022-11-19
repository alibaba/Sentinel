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
package com.alibaba.csp.sentinel.init;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Load registered init functions and execute in order.
 *
 * @author Eric Zhao
 */
public final class InitExecutor {

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * If one {@link InitFunc} throws an exception, the init process
     * will immediately be interrupted and the application will exit.
     *
     * The initialization will be executed only once.
     */
    public static void doInit() {
        //If initialized has already  return
        if (initialized.get()) return;
        //lock for initialize
        synchronized (InitExecutor.class) {
            //check again，this place is thread-safe
            if (initialized.get()) return;
            try {
                List<InitFunc> initFuncs = SpiLoader.of(InitFunc.class).loadInstanceListSorted();
                List<OrderWrapper> initList = new ArrayList<OrderWrapper>();
                for (InitFunc initFunc : initFuncs) {
                    RecordLog.info("[InitExecutor] Found init func: {}", initFunc.getClass().getCanonicalName());
                    insertSorted(initList, initFunc);
                }
                for (OrderWrapper w : initList) {
                    w.func.init();
                    RecordLog.info("[InitExecutor] Executing {} with order {}",
                            w.func.getClass().getCanonicalName(), w.order);
                }
                //initialize success
                initialized.set(true);
            } catch (Exception ex) {
                RecordLog.warn("[InitExecutor] WARN: Initialization failed", ex);
                ex.printStackTrace();
            } catch (Error error) {
                RecordLog.warn("[InitExecutor] ERROR: Initialization failed with fatal error", error);
                error.printStackTrace();
            }
        }
    }

    private static void insertSorted(List<OrderWrapper> list, InitFunc func) {
        int order = resolveOrder(func);
        int idx = 0;
        for (; idx < list.size(); idx++) {
            if (list.get(idx).getOrder() > order) {
                break;
            }
        }
        list.add(idx, new OrderWrapper(order, func));
    }

    private static int resolveOrder(InitFunc func) {
        if (!func.getClass().isAnnotationPresent(InitOrder.class)) {
            return InitOrder.LOWEST_PRECEDENCE;
        } else {
            return func.getClass().getAnnotation(InitOrder.class).value();
        }
    }

    private InitExecutor() {}

    private static class OrderWrapper {
        private final int order;
        private final InitFunc func;

        OrderWrapper(int order, InitFunc func) {
            this.order = order;
            this.func = func;
        }

        int getOrder() {
            return order;
        }

        InitFunc getFunc() {
            return func;
        }
    }
}