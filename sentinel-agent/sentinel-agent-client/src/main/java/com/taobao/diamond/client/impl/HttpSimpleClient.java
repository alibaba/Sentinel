package com.taobao.diamond.client.impl;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.alibaba.middleware.tls.JavaSSLSocketFactory;
import com.alibaba.middleware.tls.util.TlsUtil;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.config.STSConfig;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.utils.EnvUtil;
import com.taobao.diamond.utils.JSONUtils;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.TypeReference;

public class HttpSimpleClient {
    static String DIAMOND_CLIENT_VERSION = "unknown";

    static final int DIAMOND_CONNECT_TIMEOUT;

    private static volatile STSCredential stsCredential;

    static {
        String tmp = "1000"; //change timeout from 100 to 200
        try {
            tmp = System.getProperty("DIAMOND.CONNECT.TIMEOUT","1000"); //change timeout from 100 to 200
            DIAMOND_CONNECT_TIMEOUT = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            final String msg = "[http-client] invalid connect timeout:" + tmp;
            log.error("settings", "DIAMOND-XXXX", msg, e);
            throw new IllegalArgumentException(msg, e);
        }
        log.info("settings","[http-client] connect timeout:{}", DIAMOND_CONNECT_TIMEOUT);
        
		try {
			InputStream in = HttpSimpleClient.class.getClassLoader()
					.getResourceAsStream("application.properties");
			Properties props = new Properties();
			props.load(in);
			String val = null;
			val = props.getProperty("version");
			if (val != null) {
				DIAMOND_CLIENT_VERSION = val;
			}
			log.info("DIAMOND_CLIENT_VERSION:{}", DIAMOND_CLIENT_VERSION);
		} catch (Exception e) {
			log.error("500", "read application.properties", e);
		}
		
    }


    static public HttpResult httpGet(String url, List<String> headers, List<String> paramValues,
            String encoding, long readTimeoutMs, boolean isSSL) throws IOException{
    	String encodedContent = encodingParams(paramValues, encoding);
        url += (null == encodedContent) ? "" : ("?" + encodedContent);
		if (Limiter.isLimit(MD5.getInstance().getMD5String(
				new StringBuilder(url).append(encodedContent).toString()))) {
			return new HttpResult(DiamondException.CLIENT_OVER_THRESHOLD,
					"More than client-side current limit threshold");
		}
        HttpURLConnection conn = null;
        
		try {
			if (isSSL) {
				TlsUtil.replaceHostnameVerifier();
				SSLSocketFactory ssf = JavaSSLSocketFactory.createJavaSSLSocketFactory(null, null);
				HttpsURLConnection httpsConn = (HttpsURLConnection) new URL(url).openConnection();
				httpsConn.setSSLSocketFactory(ssf);
				conn = httpsConn;
			} else {
				conn = (HttpURLConnection) new URL(url).openConnection();
			}
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(DIAMOND_CONNECT_TIMEOUT > 100 ? DIAMOND_CONNECT_TIMEOUT : 100);
			conn.setReadTimeout((int) readTimeoutMs);
			List<String> newHeaders = getHeaders(url, headers, paramValues);
			setHeaders(conn, newHeaders, encoding);

			conn.connect();
			int respCode = conn.getResponseCode(); // �����ڲ���������
			String resp = null;

			if (HttpURLConnection.HTTP_OK == respCode) {
				resp = IOUtils.toString(conn.getInputStream(), encoding);
			} else {
				resp = IOUtils.toString(conn.getErrorStream(), encoding);
			}

			return new HttpResult(respCode, conn.getHeaderFields(), resp);
		} finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

	private static List<String> getHeaders(String url, List<String> headers, List<String> paramValues)
		throws IOException {
		List<String> newHeaders = getSpasHeaders(url, paramValues);
		if (headers != null) {
			newHeaders.addAll(headers);
		}
		// json���л�֧���ֶβ���ȫƥ��
		newHeaders.add("exConfigInfo");
		newHeaders.add("true");
		return newHeaders;
	}

	private static List<String> getSpasHeaders(String url, List<String> paramValues)
		throws IOException {
		List<String> newHeaders = new ArrayList<String>();
		if (ServerListManager.ADDRESS_SERVER_URL.equals(url)) {
			return newHeaders;
		}
		String accessKey;
		String secretKey;
		// STS ��ʱƾ֤��Ȩ�����ȼ����� AK/SK ��Ȩ
		if (STSConfig.getInstance().isSTSOn()) {
			STSCredential stsCredential = getSTSCredential();
			accessKey = stsCredential.accessKeyId;
			secretKey = stsCredential.accessKeySecret;

			newHeaders.add("Spas-SecurityToken");
			newHeaders.add(stsCredential.securityToken);
		} else {
			accessKey = SpasAdapter.getAk();
			secretKey = SpasAdapter.getSk();
		}
		newHeaders.add("Spas-AccessKey");
		newHeaders.add(accessKey);
		List<String> signHeaders = SpasAdapter.getSignHeaders(paramValues, secretKey);
		if (signHeaders != null) {
			newHeaders.addAll(signHeaders);
		}
		return newHeaders;
	}

	private static STSCredential getSTSCredential() throws IOException {
		boolean cacheSecurityCredentials = STSConfig.getInstance().isCacheSecurityCredentials();
		if (cacheSecurityCredentials && stsCredential != null) {
			long currentTime = System.currentTimeMillis();
			long expirationTime = stsCredential.expiration.getTime();
			int timeToRefreshInMillisecond = STSConfig.getInstance().getTimeToRefreshInMillisecond();
			if (expirationTime - currentTime > timeToRefreshInMillisecond) {
				return stsCredential;
			}
		}
		String stsResponse = getSTSResponse();
		STSCredential stsCredential = (STSCredential)JSONUtils.deserializeObject(stsResponse,
			new TypeReference<STSCredential>() {});
		HttpSimpleClient.stsCredential = stsCredential;
		log.info("getSTSCredential", "code:{}, accessKeyId:{}, lastUpdated:{}, expiration:{}", stsCredential.getCode(),
			stsCredential.getAccessKeyId(), stsCredential.getLastUpdated(), stsCredential.getExpiration());
		return stsCredential;
	}

	private static String getSTSResponse() throws IOException {
		String securityCredentials = STSConfig.getInstance().getSecurityCredentials();
		if (securityCredentials != null) {
			return securityCredentials;
		}
		String securityCredentialsUrl = STSConfig.getInstance().getSecurityCredentialsUrl();
		HttpURLConnection conn = null;
		int respCode;
		String response;
		try {
			conn = (HttpURLConnection)new URL(securityCredentialsUrl).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(DIAMOND_CONNECT_TIMEOUT > 100 ? DIAMOND_CONNECT_TIMEOUT : 100);
			conn.setReadTimeout(1000);
			conn.connect();
			respCode = conn.getResponseCode();
			String encoding = "UTF-8";
			if (HttpURLConnection.HTTP_OK == respCode) {
				response = IOUtils.toString(conn.getInputStream(), encoding);
			} else {
				response = IOUtils.toString(conn.getErrorStream(), encoding);
			}
		} catch (IOException e) {
			log.error("500", "can not get security credentials", e);
			throw e;
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
		}
		if (HttpURLConnection.HTTP_OK == respCode) {
			return response;
		}
		log.error(respCode + "", "can not get security credentials, securityCredentialsUrl:{}, response:{}",
			new Object[] {securityCredentialsUrl, response});
		throw new IOException("can not get security credentials, responseCode: " + respCode + ", response: " + response);
	}

	/**
	 * ����GET����
	 */
	static public HttpResult httpGet(String url, List<String> headers, List<String> paramValues, String encoding,
			long readTimeoutMs) throws IOException {
		return httpGet(url, headers, paramValues, encoding, readTimeoutMs, false);
	}

    /**
     * ����POST����
     * 
     * @param url
     * @param headers
     *            ����Header������Ϊnull
     * @param paramValues
     *            �������Ϊnull
     * @param encoding
     *            URL����ʹ�õ��ַ�
     * @param readTimeoutMs
     *            ��Ӧ��ʱ
     * @param isSSL
     *            �Ƿ�https
     * @return
     * @throws IOException
     */
    static public HttpResult httpPost(String url, List<String> headers, List<String> paramValues,
            String encoding, long readTimeoutMs, boolean isSSL) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);
		if (Limiter.isLimit(MD5.getInstance().getMD5String(
				new StringBuilder(url).append(encodedContent).toString()))) {
			return new HttpResult(DiamondException.CLIENT_OVER_THRESHOLD,
					"More than client-side current limit threshold");
		}
        HttpURLConnection conn = null;
        try {
        	if (isSSL) {
        		TlsUtil.replaceHostnameVerifier();
				SSLSocketFactory ssf = JavaSSLSocketFactory.createJavaSSLSocketFactory(null, null);
				HttpsURLConnection httpsConn = (HttpsURLConnection) new URL(url).openConnection();
				httpsConn.setSSLSocketFactory(ssf);
				conn = httpsConn;
			} else {
				conn = (HttpURLConnection) new URL(url).openConnection();
			}
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(DIAMOND_CONNECT_TIMEOUT > 3000 ? DIAMOND_CONNECT_TIMEOUT : 3000);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);
			List<String> newHeaders = getHeaders(url, headers, paramValues);
			setHeaders(conn, newHeaders, encoding);

            conn.getOutputStream().write(encodedContent.getBytes());

            int respCode = conn.getResponseCode(); // �����ڲ���������
            String resp = null;

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IOUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(respCode, conn.getHeaderFields(), resp);
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
    }
    
	/**
	 * ����POST����
	 * 
	 * @param url
	 * @param headers
	 *            ����Header������Ϊnull
	 * @param paramValues
	 *            �������Ϊnull
	 * @param encoding
	 *            URL����ʹ�õ��ַ�
	 * @param readTimeoutMs
	 *            ��Ӧ��ʱ
	 * @return
	 * @throws IOException
	 */
	static public HttpResult httpPost(String url, List<String> headers, List<String> paramValues, String encoding,
			long readTimeoutMs) throws IOException {
		return httpPost(url, headers, paramValues, encoding, readTimeoutMs, false);
	}

    static private void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
        if (null != headers) {
            for (Iterator<String> iter = headers.iterator(); iter.hasNext();) {
                conn.addRequestProperty(iter.next(), iter.next());
            }
        }
        conn.addRequestProperty("Client-Version", DIAMOND_CLIENT_VERSION); // TODO
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="
                + encoding);

        //
        String ts = String.valueOf(System.currentTimeMillis());
        String token = MD5.getInstance().getMD5String(ts + ServerHttpAgent.appKey);

        conn.addRequestProperty(Constants.CLIENT_APPNAME_HEADER, ServerHttpAgent.appName);
        conn.addRequestProperty(Constants.CLIENT_REQUEST_TS_HEADER, ts);
        conn.addRequestProperty(Constants.CLIENT_REQUEST_TOKEN_HEADER, token);
		conn.addRequestProperty(EnvUtil.AMORY_TAG, EnvUtil.getSelfAmorayTag());
		conn.addRequestProperty(EnvUtil.VIPSERVER_TAG, EnvUtil.getSelfVipserverTag());
		conn.addRequestProperty(EnvUtil.LOCATION_TAG, EnvUtil.getSelfLocationTag());
    }

    static private String encodingParams(List<String> paramValues, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }
        
        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext();) {
            sb.append(iter.next()).append("=");
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
    
    
    
    static public class HttpResult {
        final public int code;
        final public Map<String,List<String>> headers;
        final public String content;

		public HttpResult(int code, String content) {
			this.code = code;
			this.headers = null;
			this.content = content;
		}
		
		public HttpResult(int code, Map<String, List<String>> headers, String content) {
			this.code = code;
			this.headers = headers;
			this.content = content;
		}
    }
    
    public static String getDIAMOND_CLIENT_VERSION() {
		return DIAMOND_CLIENT_VERSION;
	}

	private static class STSCredential {
		@JsonProperty(value = "AccessKeyId")
		private String accessKeyId;
		@JsonProperty(value = "AccessKeySecret")
		private String accessKeySecret;
		@JsonProperty(value = "Expiration")
		private Date expiration;
		@JsonProperty(value = "SecurityToken")
		private String securityToken;
		@JsonProperty(value = "LastUpdated")
		private Date lastUpdated;
		@JsonProperty(value = "Code")
		private String code;

		public String getAccessKeyId() {
			return accessKeyId;
		}

		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}

		public String getAccessKeySecret() {
			return accessKeySecret;
		}

		public void setAccessKeySecret(String accessKeySecret) {
			this.accessKeySecret = accessKeySecret;
		}

		public Date getExpiration() {
			return expiration;
		}

		public void setExpiration(Date expiration) {
			this.expiration = expiration;
		}

		public String getSecurityToken() {
			return securityToken;
		}

		public void setSecurityToken(String securityToken) {
			this.securityToken = securityToken;
		}

		public Date getLastUpdated() {
			return lastUpdated;
		}

		public void setLastUpdated(Date lastUpdated) {
			this.lastUpdated = lastUpdated;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return "STSCredential{" +
				"accessKeyId='" + accessKeyId + '\'' +
				", accessKeySecret='" + accessKeySecret + '\'' +
				", expiration=" + expiration +
				", securityToken='" + securityToken + '\'' +
				", lastUpdated=" + lastUpdated +
				", code='" + code + '\'' +
				'}';
		}
	}
}
