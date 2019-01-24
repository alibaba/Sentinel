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
 * @since 1.4.0
 */
public class ConnectionDescriptorVO {

    private String address;
    private String host;

    public String getAddress() {
        return address;
    }

    public ConnectionDescriptorVO setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ConnectionDescriptorVO setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptorVO{" +
            "address='" + address + '\'' +
            ", host='" + host + '\'' +
            '}';
    }
}
