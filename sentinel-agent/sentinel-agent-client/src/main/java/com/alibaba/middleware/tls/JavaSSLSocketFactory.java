package com.alibaba.middleware.tls;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.alibaba.middleware.tls.util.TlsConstants;
import com.alibaba.middleware.tls.util.TlsUtil;

/**
 * HttpUrlConnection https 需要的SSLSocketFactory
 *
 * @author shenyu.yxl
 */
public class JavaSSLSocketFactory {

    private static SSLSocketFactory javaSsf;



    public static SSLSocketFactory createJavaSSLSocketFactory(
            Boolean clientAuth, String trustCertPath) throws SSLException {

        if (javaSsf != null) {
            return javaSsf;
        }

        return createJavaSSLSocketFactory0(clientAuth, trustCertPath);
    }

    private synchronized static SSLSocketFactory createJavaSSLSocketFactory0(
            Boolean clientAuth, String trustCertPath) throws SSLException {
        if (javaSsf != null) {
            return javaSsf;
        }

        try {

            clientAuth = clientAuth != null ? clientAuth :
                    TlsUtil.serverNeedClientAuth();
            trustCertPath = trustCertPath != null ? trustCertPath :
                    TlsUtil.clientTrustCertPath();

            TrustManager[] trustManagers = SelfTrustManager.trustManager(
                    clientAuth, trustCertPath);

            SSLContext sc = SSLContext.getInstance(TlsConstants.PROTOCOL);
            sc.init(null, trustManagers, new SecureRandom());

            SSLSocketFactory sf = sc.getSocketFactory();

            javaSsf = sf;
        } catch (Exception e) {
            throw new SSLException("Https createJavaSSLSocketFactory error ", e);
        }
        return javaSsf;

    }

}
