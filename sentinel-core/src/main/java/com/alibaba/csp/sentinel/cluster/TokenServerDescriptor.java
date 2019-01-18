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
package com.alibaba.csp.sentinel.cluster;

/**
 * A simple descriptor for Sentinel token server.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class TokenServerDescriptor {

    private final String host;
    private final int port;

    private String type = "default";

    public TokenServerDescriptor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getType() {
        return type;
    }

    public TokenServerDescriptor setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "TokenServerDescriptor{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", type='" + type + '\'' +
            '}';
    }
}
