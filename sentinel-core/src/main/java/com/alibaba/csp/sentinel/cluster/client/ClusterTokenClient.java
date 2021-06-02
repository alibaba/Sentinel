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

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.TokenService;

/**
 * Token client interface for distributed flow control.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface ClusterTokenClient extends TokenService {

    /**
     * Get descriptor of current token server.
     *
     * @return current token server if connected, otherwise null
     */
    TokenServerDescriptor currentServer();

    /**
     * Start the token client.
     *
     * @throws Exception some error occurs
     */
    void start() throws Exception;

    /**
     * Stop the token client.
     *
     * @throws Exception some error occurs
     */
    void stop() throws Exception;

    /**
     * Get state of the cluster token client.
     *
     * @return state of the cluster token client
     */
    int getState();
}