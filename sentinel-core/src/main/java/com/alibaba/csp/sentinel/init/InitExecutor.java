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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.VersionUtil;

/**
 * Load registered init functions and execute in order.
 *
 * @author Eric Zhao
 */
public final class InitExecutor {

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Make sure the fastjson (if have one) no more less than 1.2.12
     */
    private static void checkFastjson() {
        String className = "com.alibaba.fastjson.JSON";
        try {
            Class<?> clazz = Class.forName(className);
            Field versionField = clazz.getDeclaredField("VERSION");
            Object val = versionField.get(clazz);
            if (val instanceof String) {
                if (VersionUtil.fromVersionString((String) val) < 0x01021200) {
                    RecordLog.warn("[InitExecutor] Init failed, fastjson is too old (should >= 1.2.12, {0} given)", val);
                    throw new RuntimeException("fastjson is too old (should >= 1.2.12, " + val + " given)");
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            // no fastjson.jar in class path
        }
    }

    /**
     * If one {@link InitFunc} throws an exception, the init process
     * will immediately be interrupted and the application will exit.
     *
     * The initialization will be executed only once.
     */
    public static void doInit() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        checkFastjson();
        try {
            ServiceLoader<InitFunc> loader = ServiceLoader.load(InitFunc.class);
            List<OrderWrapper> initList = new ArrayList<OrderWrapper>();
            for (InitFunc initFunc : loader) {
                RecordLog.info("[InitExecutor] Found init func: " + initFunc.getClass().getCanonicalName());
                insertSorted(initList, initFunc);
            }
            for (OrderWrapper w : initList) {
                w.getFunc().init();
                RecordLog.info(String.format("[InitExecutor] Executing %s with order %d",
                    w.getFunc().getClass().getCanonicalName(), w.order));
            }
        } catch (Exception ex) {
            RecordLog.warn("[InitExecutor] WARN: Initialization failed", ex);
            ex.printStackTrace();
        } catch (Error error) {
            RecordLog.warn("[InitExecutor] ERROR: Initialization failed with fatal error", error);
            error.printStackTrace();
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
