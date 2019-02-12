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
package com.alibaba.csp.sentinel.cluster.request.data;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class FlowRequestData {

    private long flowId;
    private int count;
    private boolean priority;

    public long getFlowId() {
        return flowId;
    }

    public FlowRequestData setFlowId(long flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getCount() {
        return count;
    }

    public FlowRequestData setCount(int count) {
        this.count = count;
        return this;
    }

    public boolean isPriority() {
        return priority;
    }

    public FlowRequestData setPriority(boolean priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public String toString() {
        return "FlowRequestData{" +
            "flowId=" + flowId +
            ", count=" + count +
            ", priority=" + priority +
            '}';
    }
}
