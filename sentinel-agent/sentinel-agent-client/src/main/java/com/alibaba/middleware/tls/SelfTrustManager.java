package com.alibaba.middleware.tls;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.alibaba.middleware.tls.log.TlsLogger;
import com.alibaba.middleware.tls.util.FileUtil;

/**
 * 自定义根证书信任校验逻辑
 * @author shenyu.yxl
 *
 */
public class SelfTrustManager {

    // An insecure trust manager which will accept all tls connections.
    static TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};


	public static TrustManager[] trustManager(boolean clientAuth,
                                              String trustCertPath) {
        if (clientAuth) {
            try {
                return trustCertPath == null ? null :
                        buildSecureTrustManager(trustCertPath);
            } catch (SSLException e) {
                TlsLogger.warn("degrade trust manager as build failed, " +
                        "will trust all certs.");
                return trustAll;
            }
        } else {
            return trustAll;
        }
    }

    private static TrustManager[] buildSecureTrustManager(String trustCertPath)
            throws SSLException {
        TrustManagerFactory selfTmf = null;
        InputStream in = null;

        try {
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            selfTmf = TrustManagerFactory.getInstance(algorithm);
            
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, null);
            
            in = new FileInputStream(trustCertPath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
			
			Collection<X509Certificate> certs = (Collection<X509Certificate>)
					cf.generateCertificates(in);
			int count = 0;
		    for (Certificate cert : certs) {
		    	trustKeyStore.setCertificateEntry("cert-" + (count++), cert);
		    }
		    selfTmf.init(trustKeyStore);
            return  selfTmf.getTrustManagers();
        } catch (Exception e) {
            TlsLogger.error("build client trustManagerFactory failed", e);
            throw new SSLException(e);
        } finally {
            FileUtil.closeQuietly(in);
        }


    }
}
