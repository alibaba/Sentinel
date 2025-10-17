/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.client.ha;

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author icodening
 * @date 2022.03.08
 */
class DynamicTokenServerList {

    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("DynamicTokenServerList");
        return thread;
    });

    private final String appName;

    private final TokenServerDiscovery tokenServerDiscovery;

    private volatile List<TokenServerDescriptor> tokenServerDescriptors;

    private volatile int lastHashCode;

    private volatile boolean started;

    private final Set<TokenServerListChangeObserver> tokenServerListChangeObservers = new HashSet<>();

    DynamicTokenServerList(String appName, TokenServerDiscovery tokenServerDiscovery) {
        this.appName = appName;
        this.tokenServerDiscovery = tokenServerDiscovery;
    }

    public void registerTokenServerListChangeObserver(TokenServerListChangeObserver tokenServerListChangeObserver) {
        if (tokenServerListChangeObserver == null) {
            return;
        }
        tokenServerListChangeObservers.add(tokenServerListChangeObserver);
    }

    public List<TokenServerDescriptor> getTokenServers() {
        if (tokenServerDescriptors == null) {
            this.tokenServerDescriptors = tokenServerDiscovery.getTokenServers(appName);
            this.lastHashCode = tokenServerDescriptors.hashCode();
            start();
        }
        return this.tokenServerDescriptors;
    }

    private void start() {
        if (started) {
            return;
        }
        this.started = true;
        SCHEDULED_THREAD_POOL_EXECUTOR.schedule(new FetchTokenServerTask(), 30, TimeUnit.SECONDS);
    }

    public void stop() {
        this.started = false;
    }

    private class FetchTokenServerTask implements Runnable {

        @Override
        public void run() {
            if (!started) {
                return;
            }
            try {
                List<TokenServerDescriptor> tokenServers = DynamicTokenServerList.this.tokenServerDiscovery.getTokenServers(appName);
                if (tokenServers.hashCode() != DynamicTokenServerList.this.lastHashCode) {
                    DynamicTokenServerList.this.tokenServerDescriptors = tokenServers;
                    DynamicTokenServerList.this.lastHashCode = tokenServers.hashCode();
                    for (TokenServerListChangeObserver tokenServerListChangeObserver : DynamicTokenServerList.this.tokenServerListChangeObservers) {
                        tokenServerListChangeObserver.onTokenServerListChange(DynamicTokenServerList.this.tokenServerDescriptors);
                    }
                }
            } catch (Throwable ex) {
                RecordLog.warn("[DynamicTokenServerList] Failed to fetch remote token servers", ex);
            } finally {
                SCHEDULED_THREAD_POOL_EXECUTOR.schedule(new FetchTokenServerTask(), 30, TimeUnit.SECONDS);
            }
        }
    }

}
