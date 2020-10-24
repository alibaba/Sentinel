/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.SpiLoader;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Manager for gateway API definitions.
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class GatewayApiDefinitionManager {

    private static final Map<String, ApiDefinition> API_MAP = new ConcurrentHashMap<>();

    private static final ApiDefinitionPropertyListener LISTENER = new ApiDefinitionPropertyListener();
    private static SentinelProperty<Set<ApiDefinition>> currentProperty = new DynamicSentinelProperty<>();

    /**
     * The map keeps all found ApiDefinitionChangeObserver (class name as key).
     */
    private static final Map<String, ApiDefinitionChangeObserver> API_CHANGE_OBSERVERS = new ConcurrentHashMap<>();

    static {
        try {
            currentProperty.addListener(LISTENER);
            initializeApiChangeObserverSpi();
        } catch (Throwable ex) {
            RecordLog.warn("[GatewayApiDefinitionManager] Failed to initialize", ex);
            ex.printStackTrace();
        }
    }

    private static void initializeApiChangeObserverSpi() {
        List<ApiDefinitionChangeObserver> listeners = SpiLoader.loadInstanceList(ApiDefinitionChangeObserver.class);
        for (ApiDefinitionChangeObserver e : listeners) {
            API_CHANGE_OBSERVERS.put(e.getClass().getCanonicalName(), e);
            RecordLog.info("[GatewayApiDefinitionManager] ApiDefinitionChangeObserver added: {}"
                , e.getClass().getCanonicalName());
        }
    }

    public static void register2Property(SentinelProperty<Set<ApiDefinition>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[GatewayApiDefinitionManager] Registering new property to gateway API definition manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load given gateway API definitions and apply to downstream observers.
     *
     * @param apiDefinitions set of gateway API definitions
     * @return true if updated, or else false
     */
    public static boolean loadApiDefinitions(Set<ApiDefinition> apiDefinitions) {
        return currentProperty.updateValue(apiDefinitions);
    }

    public static ApiDefinition getApiDefinition(final String apiName) {
        if (apiName == null) {
            return null;
        }
        return API_MAP.get(apiName);
    }

    public static Set<ApiDefinition> getApiDefinitions() {
        return new HashSet<>(API_MAP.values());
    }

    private static final class ApiDefinitionPropertyListener implements PropertyListener<Set<ApiDefinition>> {

        @Override
        public void configUpdate(Set<ApiDefinition> set) {
            applyApiUpdateInternal(set);
            RecordLog.info("[GatewayApiDefinitionManager] Api definition updated: {}", API_MAP);
        }

        @Override
        public void configLoad(Set<ApiDefinition> set) {
            applyApiUpdateInternal(set);
            RecordLog.info("[GatewayApiDefinitionManager] Api definition loaded: {}", API_MAP);
        }

        private static synchronized void applyApiUpdateInternal(Set<ApiDefinition> set) {
            if (set == null || set.isEmpty()) {
                API_MAP.clear();
                notifyDownstreamListeners(new HashSet<ApiDefinition>());
                return;
            }
            Map<String, ApiDefinition> map = new HashMap<>(set.size());
            Set<ApiDefinition> validSet = new HashSet<>();
            for (ApiDefinition definition : set) {
                if (isValidApi(definition)) {
                    map.put(definition.getApiName(), definition);
                    validSet.add(definition);
                }
            }

            API_MAP.clear();
            API_MAP.putAll(map);

            // propagate to downstream.
            notifyDownstreamListeners(validSet);
        }
    }

    private static void notifyDownstreamListeners(/*@Valid*/ final Set<ApiDefinition> definitions) {
        try {
            for (Map.Entry<?, ApiDefinitionChangeObserver> entry : API_CHANGE_OBSERVERS.entrySet()) {
                entry.getValue().onChange(definitions);
            }
        } catch (Exception ex) {
            RecordLog.warn("[GatewayApiDefinitionManager] WARN: failed to notify downstream api listeners", ex);
        }
    }

    public static boolean isValidApi(ApiDefinition apiDefinition) {
        return apiDefinition != null && StringUtil.isNotBlank(apiDefinition.getApiName())
            && apiDefinition.getPredicateItems() != null;
    }

    static void addApiChangeListener(ApiDefinitionChangeObserver listener) {
        AssertUtil.notNull(listener, "listener cannot be null");
        API_CHANGE_OBSERVERS.put(listener.getClass().getCanonicalName(), listener);
    }

    static void removeApiChangeListener(Class<?> clazz) {
        AssertUtil.notNull(clazz, "class cannot be null");
        API_CHANGE_OBSERVERS.remove(clazz.getCanonicalName());
    }
}
