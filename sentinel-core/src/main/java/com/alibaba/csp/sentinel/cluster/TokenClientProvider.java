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
package com.alibaba.csp.sentinel.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * Provider for a universal {@link ClusterTokenClient} instance.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class TokenClientProvider {

    private static ClusterTokenClient client = null;

    private static final ServiceLoader<ClusterTokenClient> LOADER = ServiceLoader.load(ClusterTokenClient.class);

    static {
        // Not strictly thread-safe, but it's OK since it will be resolved only once.
        resolveTokenClientInstance();
    }

    public static ClusterTokenClient getClient() {
        return client;
    }

    private static void resolveTokenClientInstance() {
        List<ClusterTokenClient> clients = new ArrayList<ClusterTokenClient>();
        for (ClusterTokenClient client : LOADER) {
            clients.add(client);
        }

        if (!clients.isEmpty()) {
            // Get first.
            client = clients.get(0);
            RecordLog.info("[TokenClientProvider] Token client resolved: " + client.getClass().getCanonicalName());
        } else {
            RecordLog.warn("[TokenClientProvider] No existing token client, resolve failed");
        }
    }

    private TokenClientProvider() {}
}
