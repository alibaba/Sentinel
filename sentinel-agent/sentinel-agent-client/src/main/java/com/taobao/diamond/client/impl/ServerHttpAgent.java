package com.taobao.diamond.client.impl;

import com.alibaba.middleware.tls.util.TlsUtil;
import com.taobao.diamond.client.impl.HttpSimpleClient.HttpResult;
import com.taobao.diamond.utils.AppNameUtils;
import com.taobao.diamond.utils.StringUtils;
import com.taobao.middleware.logger.support.LoggerHelper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import static com.taobao.diamond.client.impl.DiamondEnv.log;

/**
 * �������ȼ�˳���server������ֱ��ĳ��server����ɹ���
 * 
 * ճ�ԣ��������ĳ��server�ɹ�����ô�¸�����Ҳ�����ȷ����server��
 */
public class ServerHttpAgent {
    public static final String appKey;
    public static final String appName;

    public static String domainName;
    
	public static String addressPort;

    static {
        // �ͻ��������Ϣ
        appKey = System.getProperty("diamond.client.appKey", "");
        
        appName = AppNameUtils.getAppName();
        
		domainName = System.getenv("address_server_domain");
		if (StringUtils.isBlank(domainName)) {
			domainName = System.getProperty("address.server.domain");
			if (StringUtils.isBlank(domainName)) {
				domainName = System.getProperty("acm.endpoint", "jmenv.tbsite.net");
			}
		}

		String envAddressPort = System.getenv("address_server_port");
		if (StringUtils.isBlank(envAddressPort)) {
			addressPort = System.getProperty("address.server.port", "8080");
		} else {
			addressPort = envAddressPort;
		}

		log.info("settings",
				"address-server domain:{} ,address-server port:{}", domainName,
				addressPort);
    }

    ServerHttpAgent(ServerListManager mgr) {
        serverListMgr = mgr;
    }

	public static void setDomainName(String domainName) {
		if (StringUtils.isNotEmpty(domainName)) {
			ServerHttpAgent.domainName = domainName;
			log.info("setDomainName", "address-server domain:{} ,address-server port:{}", domainName, addressPort);
		}
	}
    
    /**
     * @param path
     *            �����webӦ�ø���/��ͷ
     * @param headers
     * @param paramValues
     * @param encoding
     * @param readTimeoutMs
     * @return
     * @throws IOException
     */
    public HttpResult httpGet(String path, List<String> headers, List<String> paramValues,
            String encoding, long readTimeoutMs) throws IOException {
        final long endTime = System.currentTimeMillis() + readTimeoutMs;
		String port = "8080";
		boolean isSSL = false;
		if (TlsUtil.tlsEnable()) {
			port = "443";
			isSSL = true;
		}
        if (null != currentServerIp) {
            try {
				port = serverListMgr.getPortByIp(currentServerIp);
				HttpResult result = HttpSimpleClient.httpGet(
						getUrl(currentServerIp, port, path, isSSL),
                        headers, paramValues, encoding, readTimeoutMs, isSSL);
				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("Diamond ConnectException", "currentServerIp:{},port:{}. httpCode:",
							new Object[] { currentServerIp, port, result.code });
				} else {
					return result;
				}
            } catch (ConnectException ce) {
            	log.error("Diamond ConnectException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (SocketTimeoutException stoe) {
            	log.error("Diamond  SocketTimeoutException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (IOException ioe) {
            	log.error("Diamond  IOException", "currentServerIp:{},port:{}",
						new Object[] { currentServerIp, port });
                throw ioe;
            }
        }

        for (Iterator<String> serverIter = serverListMgr.iterator(); serverIter.hasNext();) {
            long timeout = endTime - System.currentTimeMillis();
            if (timeout <= 0) {
            	if(null!= currentServerIp){
            		log.error("the currentServerIp  which happened IOException in get(timeout) is: ", currentServerIp);
            	}
                currentServerIp = serverIter.next(); // previous node performs slowly
                //log.info("the currentServerIp  in get() after serverIter.next is: ", currentServerIp);
                throw new IOException("timeout");
            }

            String ip = serverIter.next();
            try {
				port = serverListMgr.getPortByIp(ip);
                HttpResult result = HttpSimpleClient.httpGet(getUrl(ip, port, path, isSSL), headers,
                        paramValues, encoding, timeout, isSSL);
                
                // log.info("the currentServerIp in get() is: ", currentServerIp);
				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("Diamond ConnectException", "currentServerIp:{},port:{}. httpCode:",
							new Object[] { currentServerIp, port, result.code });
				} else {
					currentServerIp = ip;
					return result;
				}
            } catch (ConnectException ce) {
            	log.error("Diamond ConnectException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (SocketTimeoutException stoe) {
            	log.error("Diamond  SocketTimeoutException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (IOException ioe) {
            	log.error("Diamond  IOException", "currentServerIp:{},port:{}",
						new Object[] { currentServerIp, port });
                throw ioe;
            }
        }
        log.error("Diamond-0002", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0002", "��������","no available server"));
        throw new ConnectException("no available server");
    }

    public HttpResult httpPost(String path, List<String> headers, List<String> paramValues,
            String encoding, long readTimeoutMs) throws IOException {
		final long endTime = System.currentTimeMillis() + readTimeoutMs;
		String port = "8080";
		boolean isSSL = false;
		if (TlsUtil.tlsEnable()) {
			port = "443";
			isSSL = true;
		}
        if (null != currentServerIp) {
            try {
				port = serverListMgr.getPortByIp(currentServerIp);
				HttpResult result = HttpSimpleClient.httpPost(getUrl(currentServerIp, port, path, isSSL), headers, paramValues,
						encoding, readTimeoutMs, isSSL);
				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("Diamond ConnectException", "currentServerIp:{},port:{}. httpCode:",
							new Object[] { currentServerIp, port, result.code });
				} else {
					return result;
				}
            } catch (ConnectException ce) {
				log.error("Diamond ConnectException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (SocketTimeoutException stoe) {
				log.error("Diamond  SocketTimeoutException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (IOException ioe) {
				log.error("Diamond  IOException", "currentServerIp:{},port:{}",
						new Object[] { currentServerIp, port });
                throw ioe;
            }
        }

        for (Iterator<String> serverIter = serverListMgr.iterator(); serverIter.hasNext();) {
            long timeout = endTime - System.currentTimeMillis();
            if (timeout <= 0) {
            	if(null!= currentServerIp){
            		log.error("the currentServerIp  which happened IOException(timeout) in post is: ", currentServerIp);
            	}
                currentServerIp = serverIter.next(); // previous node performs slowly
               // log.info("the currentServerIp in post() after serverIter.next is: ", currentServerIp);
                throw new IOException("timeout");
            }

            String ip = serverIter.next();
            try {
            	port = serverListMgr.getPortByIp(ip);
                HttpResult result = HttpSimpleClient.httpPost(getUrl(ip, port, path, isSSL), headers,
                        paramValues, encoding, timeout, isSSL);
                
                //log.info("the currentServerIp in post is: ", currentServerIp);
                if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("Diamond ConnectException", "currentServerIp:{},port:{}. httpCode:",
							new Object[] { currentServerIp, port, result.code });
				} else {
					currentServerIp = ip;
					return result;
				}
            } catch (ConnectException ce) {
            	log.error("Diamond ConnectException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (SocketTimeoutException stoe) {
            	log.error("Diamond  SocketTimeoutException",
						"currentServerIp:{},port:{}", new Object[] {
								currentServerIp, port });
            } catch (IOException ioe) {
            	log.error("Diamond  IOException", "currentServerIp:{},port:{}",
						new Object[] { currentServerIp, port });
                throw ioe;
            }
        }
        log.error("Diamond-0002", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0002", "��������","no available server"));
        throw new ConnectException("no available server");
    }

    // relativePath�����webӦ�ø�·������/��ͷ
	static String getUrl(String ip, String port, String relativePath, boolean isSSL) {
		String httpPrefix = "http://";
		if (isSSL) {
			httpPrefix = "https://";
		}
		return httpPrefix + ip + ":" + port + "/diamond-server" + relativePath;
	}
	
	static String getUrl(String ip, String port, String relativePath) {
		return getUrl(ip, port, relativePath, false);
	}
    
    /**
     * ��Ⱥ�������б?�ͱ仯������currentServerIp
     */
    public void reSetCurrentServerIp(){
    	if(currentServerIp!=null)
    		currentServerIp = null;
    }

	public String getCurrentServerIp() {
		return currentServerIp;
	}

    public static String getAppname() {
		return appName;
	}
	
    // =================
    final ServerListManager serverListMgr;
    volatile String currentServerIp;

}
