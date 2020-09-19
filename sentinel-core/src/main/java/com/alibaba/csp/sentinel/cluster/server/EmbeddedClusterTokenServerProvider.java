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
package com.alibaba.csp.sentinel.cluster.server;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class EmbeddedClusterTokenServerProvider {

    private static EmbeddedClusterTokenServer server = null;

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        EmbeddedClusterTokenServer s = SpiLoader.loadFirstInstance(EmbeddedClusterTokenServer.class);
        if (s == null) {
            RecordLog.warn("[EmbeddedClusterTokenServerProvider] No existing cluster token server, cluster server mode will not be activated");
        } else {
            server = s;
            RecordLog.info("[EmbeddedClusterTokenServerProvider] Cluster token server resolved: {}",
                server.getClass().getCanonicalName());
        }
    }

    public static EmbeddedClusterTokenServer getServer() {
        return server;
    }

    public static boolean isServerSpiAvailable() {
        return getServer() != null;
    }

    private EmbeddedClusterTokenServerProvider() {}
}
