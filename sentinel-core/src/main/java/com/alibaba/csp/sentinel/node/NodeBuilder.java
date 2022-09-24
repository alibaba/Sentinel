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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * Builds new {@link DefaultNode} and {@link ClusterNode}.
 *
 * @author qinan.qn
 */
@Deprecated
public interface NodeBuilder {

    /**
     * Create a new {@link DefaultNode} as tree node.
     *
     * @param id resource
     * @param clusterNode the cluster node of the provided resource
     * @return new created tree node
     */
    DefaultNode buildTreeNode(ResourceWrapper id, ClusterNode clusterNode);

    /**
     * Create a new {@link ClusterNode} as universal statistic node for a single resource.
     *
     * @return new created cluster node
     */
    ClusterNode buildClusterNode();
}
