/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.model.impl;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;

/**
 * Event published when cluster state changed.
 *
 * @author Daydreamer-ia
 */
public class ClusterFlowStateEvent extends SentinelEvent {

    /**
     * origin mode, from static field about cluster state in {@link com.alibaba.csp.sentinel.cluster.ClusterStateManager}.
     */
    private final int originMode;

    /**
     * now mode, from static field about cluster state in {@link com.alibaba.csp.sentinel.cluster.ClusterStateManager}.
     */
    private final int nowMode;

    public ClusterFlowStateEvent(int originMode, int nowMode) {
        this.originMode = originMode;
        this.nowMode = nowMode;
    }

    public int getOriginMode() {
        return originMode;
    }

    public int getNowMode() {
        return nowMode;
    }
}
