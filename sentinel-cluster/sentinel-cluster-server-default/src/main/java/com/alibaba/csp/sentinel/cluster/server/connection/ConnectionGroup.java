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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * The connection group stores connection set for a specific namespace.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ConnectionGroup {

    private final String namespace;

    private final Set<ConnectionDescriptor> connectionSet = new CopyOnWriteArraySet<>();
    private final AtomicInteger connectedCount = new AtomicInteger();

    public ConnectionGroup(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        this.namespace = namespace;
    }

    public ConnectionGroup() {
        this(ServerConstants.DEFAULT_NAMESPACE);
    }

    public ConnectionGroup addConnection(String address) {
        AssertUtil.notEmpty(address, "address cannot be empty");

        String[] ip = address.split(":");
        String host;
        if (ip != null && ip.length >= 1) {
            host = ip[0];
        } else {
            host = address;
        }
        boolean newAdded = connectionSet.add(new ConnectionDescriptor().setAddress(address).setHost(host));
        if (newAdded) {
            connectedCount.incrementAndGet();
        }

        return this;
    }

    public ConnectionGroup removeConnection(String address) {
        AssertUtil.notEmpty(address, "address cannot be empty");

        if (connectionSet.remove(new ConnectionDescriptor().setAddress(address))) {
            connectedCount.decrementAndGet();
        }

        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public Set<ConnectionDescriptor> getConnectionSet() {
        return connectionSet;
    }

    public int getConnectedCount() {
        return connectedCount.get();
    }
}
