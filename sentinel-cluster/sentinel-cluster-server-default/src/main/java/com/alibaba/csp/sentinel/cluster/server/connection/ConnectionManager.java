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
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Manager for namespace-scope {@link ConnectionGroup}.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ConnectionManager {

    /**
     * Connection map (namespace, connection).
     */
    private static final Map<String, ConnectionGroup> CONN_MAP = new ConcurrentHashMap<>();
    /**
     * namespace map (address, namespace).
     */
    private static final Map<String, String> NAMESPACE_MAP = new ConcurrentHashMap<>();

    /**
     * Get connected count for specific namespace.
     *
     * @param namespace namespace to check
     * @return connected count for specific namespace
     */
    public static int getConnectedCount(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace should not be empty");
        ConnectionGroup group = CONN_MAP.get(namespace);
        return group == null ? 0 : group.getConnectedCount();
    }

    public static ConnectionGroup getOrCreateGroup(String namespace) {
        AssertUtil.assertNotBlank(namespace, "namespace should not be empty");
        ConnectionGroup group = CONN_MAP.get(namespace);
        if (group == null) {
            synchronized (CREATE_LOCK) {
                if ((group = CONN_MAP.get(namespace)) == null) {
                    group = new ConnectionGroup(namespace);
                    CONN_MAP.put(namespace, group);
                }
            }
        }
        return group;
    }

    public static void removeConnection(String address) {
        AssertUtil.assertNotBlank(address, "address should not be empty");
        String namespace = NAMESPACE_MAP.get(address);
        if (namespace != null) {
            ConnectionGroup group = CONN_MAP.get(namespace);
            if (group == null) {
                return;
            }
            group.removeConnection(address);
            RecordLog.info("[ConnectionManager] Client <{}> disconnected and removed from namespace <{}>", address, namespace);
        }
        NAMESPACE_MAP.remove(address);
    }

    public static void removeConnection(String namespace, String address) {
        AssertUtil.assertNotBlank(namespace, "namespace should not be empty");
        AssertUtil.assertNotBlank(address, "address should not be empty");
        ConnectionGroup group = CONN_MAP.get(namespace);
        if (group == null) {
            return;
        }
        group.removeConnection(address);
        NAMESPACE_MAP.remove(address);
        RecordLog.info("[ConnectionManager] Client <{}> disconnected and removed from namespace <{}>", address, namespace);
    }

    public static ConnectionGroup addConnection(String namespace, String address) {
        AssertUtil.assertNotBlank(namespace, "namespace should not be empty");
        AssertUtil.assertNotBlank(address, "address should not be empty");
        ConnectionGroup group = getOrCreateGroup(namespace);
        group.addConnection(address);
        NAMESPACE_MAP.put(address, namespace);
        RecordLog.info("[ConnectionManager] Client <{}> registered with namespace <{}>", address, namespace);
        return group;
    }

    public static ConnectionGroup getOrCreateConnectionGroup(String namespace) {
        AssertUtil.assertNotBlank(namespace, "namespace should not be empty");
        ConnectionGroup group = getOrCreateGroup(namespace);
        return group;
    }

    public static ConnectionGroup getConnectionGroup(String namespace) {
        AssertUtil.assertNotBlank(namespace, "namespace should not be empty");
        ConnectionGroup group = CONN_MAP.get(namespace);
        return group;
    }

    public static boolean isClientOnline(String address){
        return NAMESPACE_MAP.containsKey(address);
    }
    static void clear() {
        CONN_MAP.clear();
        NAMESPACE_MAP.clear();
    }

    private static final Object CREATE_LOCK = new Object();

    private ConnectionManager() {}
}
