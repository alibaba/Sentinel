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
package com.alibaba.csp.sentinel.cluster.response.data;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class FlowTokenResponseData {

    private int remainingCount;
    private int waitInMs;

    public int getRemainingCount() {
        return remainingCount;
    }

    public FlowTokenResponseData setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
        return this;
    }

    public int getWaitInMs() {
        return waitInMs;
    }

    public FlowTokenResponseData setWaitInMs(int waitInMs) {
        this.waitInMs = waitInMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlowTokenResponseData)) {
            return false;
        }
        FlowTokenResponseData that = (FlowTokenResponseData) o;
        return this.remainingCount == that.remainingCount && this.waitInMs == that.waitInMs;
    }

    @Override
    public int hashCode() {
        int result = remainingCount;
        result = 31 * result + waitInMs;
        return result;
    }

    @Override
    public String toString() {
        return "FlowTokenResponseData{" +
                "remainingCount=" + remainingCount +
                ", waitInMs=" + waitInMs +
                '}';
    }
}
