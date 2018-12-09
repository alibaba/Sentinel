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
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.util.Objects;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ConnectionDescriptor {

    private String address;
    private String host;

    public String getAddress() {
        return address;
    }

    public ConnectionDescriptor setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ConnectionDescriptor setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ConnectionDescriptor that = (ConnectionDescriptor)o;

        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptor{" +
            "address='" + address + '\'' +
            ", host='" + host + '\'' +
            '}';
    }
}
