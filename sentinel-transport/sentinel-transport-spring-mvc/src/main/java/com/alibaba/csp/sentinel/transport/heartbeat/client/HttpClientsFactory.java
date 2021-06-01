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
package com.alibaba.csp.sentinel.transport.heartbeat.client;

import com.alibaba.csp.sentinel.transport.endpoint.Protocol;
import com.alibaba.csp.sentinel.transport.ssl.SslFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Leo Li
 */
public class HttpClientsFactory {

    private static class SslConnectionSocketFactoryInstance {
        private static final SSLConnectionSocketFactory SSL_CONNECTION_SOCKET_FACTORY = new SSLConnectionSocketFactory(SslFactory.getSslConnectionSocketFactory(), NoopHostnameVerifier.INSTANCE);
    }

    public static CloseableHttpClient getHttpClientsByProtocol(Protocol protocol) {
        return protocol == Protocol.HTTP ? HttpClients.createDefault() : HttpClients.custom().
                setSSLSocketFactory(SslConnectionSocketFactoryInstance.SSL_CONNECTION_SOCKET_FACTORY).build();
    }
}
