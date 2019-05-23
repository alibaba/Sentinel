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
package com.alibaba.csp.sentinel.cluster.server.processor;

import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;

/**
 * Interface of cluster request processor.
 *
 * @param <T> type of request body
 * @param <R> type of response body
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface RequestProcessor<T, R> {

    /**
     * Process the cluster request.
     *
     * @param request Sentinel cluster request
     * @return the response after processed
     */
    ClusterResponse<R> processRequest(ClusterRequest<T> request);
}
