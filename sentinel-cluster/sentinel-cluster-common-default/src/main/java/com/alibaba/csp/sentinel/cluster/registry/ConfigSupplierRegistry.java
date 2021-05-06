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
package com.alibaba.csp.sentinel.cluster.registry;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ConfigSupplierRegistry {

    /**
     * The default namespace supplier provides appName as namespace.
     */
    private static final Supplier<String> DEFAULT_APP_NAME_SUPPLIER = new Supplier<String>() {
        @Override
        public String get() {
            return AppNameUtil.getAppName();
        }
    };
    /**
     * Registered namespace supplier.
     */
    private static Supplier<String> namespaceSupplier = DEFAULT_APP_NAME_SUPPLIER;

    /**
     * Get the registered namespace supplier.
     *
     * @return the registered namespace supplier
     */
    public static Supplier<String> getNamespaceSupplier() {
        return namespaceSupplier;
    }

    public static void setNamespaceSupplier(Supplier<String> namespaceSupplier) {
        AssertUtil.notNull(namespaceSupplier, "namespaceSupplier cannot be null");
        ConfigSupplierRegistry.namespaceSupplier = namespaceSupplier;
        RecordLog.info("[ConfigSupplierRegistry] New namespace supplier provided, current supplied: {}",
            namespaceSupplier.get());
    }

    private ConfigSupplierRegistry() {}
}
