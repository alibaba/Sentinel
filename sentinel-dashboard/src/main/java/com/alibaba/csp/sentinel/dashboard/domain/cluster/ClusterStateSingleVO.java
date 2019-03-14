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
package com.alibaba.csp.sentinel.dashboard.domain.cluster;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterStateSingleVO {

    private String address;
    private Integer mode;
    private String target;

    public String getAddress() {
        return address;
    }

    public ClusterStateSingleVO setAddress(String address) {
        this.address = address;
        return this;
    }

    public Integer getMode() {
        return mode;
    }

    public ClusterStateSingleVO setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public ClusterStateSingleVO setTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterStateSingleVO{" +
            "address='" + address + '\'' +
            ", mode=" + mode +
            ", target='" + target + '\'' +
            '}';
    }
}
