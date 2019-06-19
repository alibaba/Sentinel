/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.request.data;

import java.util.Map;
import java.util.Set;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchParamFlowRequestData {

    private Set<Long> flowIds;
    private int count;
    /**
     * Pair: (paramIdx, paramObj)
     */
    private Map<Integer, Object> paramMap;

    public Set<Long> getFlowIds() {
        return flowIds;
    }

    public BatchParamFlowRequestData setFlowIds(Set<Long> flowIds) {
        this.flowIds = flowIds;
        return this;
    }

    public int getCount() {
        return count;
    }

    public BatchParamFlowRequestData setCount(int count) {
        this.count = count;
        return this;
    }

    public Map<Integer, Object> getParamMap() {
        return paramMap;
    }

    public BatchParamFlowRequestData setParamMap(Map<Integer, Object> paramMap) {
        this.paramMap = paramMap;
        return this;
    }

    @Override
    public String toString() {
        return "BatchParamFlowRequestData{" +
            "flowIds=" + flowIds +
            ", count=" + count +
            ", paramMap=" + paramMap +
            '}';
    }
}
