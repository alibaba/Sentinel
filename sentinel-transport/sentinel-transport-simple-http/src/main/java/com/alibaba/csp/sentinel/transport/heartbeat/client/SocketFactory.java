package com.alibaba.csp.sentinel.transport.heartbeat.client;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.function.Protocol;

/**
 * @author Leo Li
 */
public class SocketFactory {
    private static X509TrustManager x509TrustManager;
    private static SSLContext sslContext;
    private static SSLSocketFactory sslSocketFactory;

    static {
        try {
            x509TrustManager = new X509TrustManager() {
                public boolean isServerTrusted(X509Certificate[] certs) {
                    return true;
                }

                public boolean isClientTrusted(X509Certificate[] certs) {
                    return true;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { x509TrustManager }, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            RecordLog.error("get ssl socket factory error", e);
        }
    }

    public static Socket getSocket(Protocol protocol) throws IOException {
        return protocol == Protocol.HTTP ? new Socket() : sslSocketFactory.createSocket();
    }
}
