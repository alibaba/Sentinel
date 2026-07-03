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
