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
package com.alibaba.csp.sentinel.cluster.client.config;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;

/**
 * @author Eric Zhao
 */
public final class ClusterClientConfigManager {

    private static volatile String serverIp;
    private static volatile int serverPort = ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT;
    private static volatile int requestTimeout = ClusterConstants.DEFAULT_REQUEST_TIMEOUT;

    private ClusterClientConfigManager() {}
}
