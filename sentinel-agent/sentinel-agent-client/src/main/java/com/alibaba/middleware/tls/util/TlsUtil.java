package com.alibaba.middleware.tls.util;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.alibaba.middleware.tls.SelfHostnameVerifier;
/**
 * @author shenyu.yxl
 *
 */
public class TlsUtil {

	private static boolean defaultTlsEnable=false;
	
    public static boolean tlsTest() {
			return Boolean.parseBoolean(System.getProperty(TlsConstants.TLS_TEST));
	}
	 
    public static boolean tlsEnable() {
		return Boolean.parseBoolean(System.getProperty(TlsConstants.TLS_ENABLE,String.valueOf(defaultTlsEnable)));
	}
    
    public static void changeTlsEnable(boolean defaultTlsEnable) {
    	TlsUtil.defaultTlsEnable=defaultTlsEnable;
  	}

    public static boolean twoWayAuth() {
		return Boolean.parseBoolean(System.getProperty(TlsConstants.TWO_WAY_AUTH));
	}
    
    public static boolean serverNeedClientAuth() {
		return Boolean.parseBoolean(System.getProperty(TlsConstants.CLIENT_AUTH));
	}
    
    public static String clientCertPath() {
		return System.getProperty(TlsConstants.CLIENT_CERTPATH);
	}
    
    public static String clientKeyPath() {
 		return System.getProperty(TlsConstants.CLIENT_KEYPATH);
 	}
    
    public static String clientTrustCertPath() {
 		return System.getProperty(TlsConstants.CLIENT_TRUST_CERT);
 	}
    
    public static boolean clientNeedServerAuth() {
 		return Boolean.parseBoolean(System.getProperty(TlsConstants.SERVER_AUTH));
 	}
    
    public static String serverCertPath() {
 		return System.getProperty(TlsConstants.SERVER_CERTPATH);
 	}
     
     public static String serverKeyPath() {
  		return System.getProperty(TlsConstants.SERVER_KEYPATH);
  	}
     
     public static String serverTrustCertPath() {
  		return System.getProperty(TlsConstants.SERVER_TRUST_CERT);
  	}

	public static void replaceHostnameVerifier() {
		final HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		HttpsURLConnection.setDefaultHostnameVerifier(new SelfHostnameVerifier(hv));
	}

	public static String getDecryptionClazzName() {
        String decryptionName = "decryption.strategy.clazz";
        String clazzName = "com.alibaba.middleware.decrypt.impl.ProxySSLDecryptionStrategy";

        return System.getProperty(decryptionName, clazzName);
    }
    
    
}
