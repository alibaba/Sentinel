package com.alibaba.csp.sentinel.transport.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Leo Li
 */
public class SslFactory {

    private static class SslContextInstance {
        private static final SSLContext SSL_CONTEXT = initSslContext();
    }

    private static SSLContext initSslContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            X509TrustManager x509TrustManager = new X509TrustManager() {
                public boolean isServerTrusted(X509Certificate[] certs) {
                    return true;
                }

                public boolean isClientTrusted(X509Certificate[] certs) {
                    return true;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { x509TrustManager }, null);
        } catch (Exception e) {
            RecordLog.error("get ssl socket factory error", e);
        }
        return sslContext;
    }

    public static SSLContext getSslConnectionSocketFactory() {
        return SslContextInstance.SSL_CONTEXT;
    }
}
