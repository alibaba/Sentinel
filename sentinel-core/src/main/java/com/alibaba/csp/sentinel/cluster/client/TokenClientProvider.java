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
package com.alibaba.csp.sentinel.cluster.client;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiLoader;

/**
 * Provider for a universal {@link ClusterTokenClient} instance.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class TokenClientProvider {

    private static ClusterTokenClient client = null;

    static {
        // Not strictly thread-safe, but it's OK since it will be resolved only once.
        resolveTokenClientInstance();
    }

    public static ClusterTokenClient getClient() {
        return client;
    }

    private static void resolveTokenClientInstance() {
        ClusterTokenClient resolvedClient = SpiLoader.of(ClusterTokenClient.class).loadFirstInstance();
        if (resolvedClient == null) {
            RecordLog.info(
                "[TokenClientProvider] No existing cluster token client, cluster client mode will not be activated");
        } else {
            client = resolvedClient;
            RecordLog.info("[TokenClientProvider] Cluster token client resolved: {}",
                client.getClass().getCanonicalName());
        }
    }

    public static boolean isClientSpiAvailable() {
        return getClient() != null;
    }

    private TokenClientProvider() {}
}
