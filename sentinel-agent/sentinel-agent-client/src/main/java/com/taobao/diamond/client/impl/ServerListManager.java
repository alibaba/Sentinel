package com.taobao.diamond.client.impl;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.middleware.tls.util.TlsUtil;
import com.taobao.diamond.client.impl.EventDispatcher.ServerlistChangeEvent;
import com.taobao.diamond.client.impl.HttpSimpleClient.HttpResult;
import com.taobao.diamond.utils.MockUtil;
import com.taobao.diamond.utils.EnvUtil;
import com.taobao.diamond.utils.StringUtils;
import com.taobao.middleware.logger.support.LoggerHelper;


/**
 * ����ʱ������ʱ���ڻ�ȡ��ַ�б?����ʱ�ò�����ַ�б?����˳���
 * ֻ��һ������jmenv.tbsite.net
 * 
 * ���صĵ�ַ�������ͬ�����ȡ�
 * 
 * @author jiuRen
 */
public class ServerListManager {
    
    public ServerListManager() {
        isFixed = false;
        isStarted = false;
        name = DEFAULT_NAME;
    }

    public ServerListManager(List<String> fixed) {
        isFixed = true;
        isStarted = true;
        serverUrls = new ArrayList<String>(fixed);
        name = FIXED_NAME + "-" +  getFixedNameSuffix(fixed.toArray(new String[fixed.size()]));
    }

    public ServerListManager(String host, int port) {
        isFixed = false;
        isStarted = false;
        name = CUSTOM_NAME + "-" + host + "-" + port;
        ADDRESS_SERVER_URL = String.format("http://%s:%d/diamond-server/diamond", host, port);
    }

    public synchronized void start() {
    	if (MockUtil.isMock()) {
			return;
		}
    	
        if (isStarted || isFixed) {
            return;
        }

        GetServerListTask getServersTask = new GetServerListTask(ADDRESS_SERVER_URL);
        for (int i = 0; i < 5 && serverUrls.isEmpty(); ++i) {
            getServersTask.run();
            try {
				Thread.sleep((i + 1) * 100L);
            } catch (Exception e) {
            }
        }

		if (serverUrls.isEmpty()) {
			log.error("Diamond-0008", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0008", "��������",
					"fail to get diamond-server serverlist! env:" + name + ", not connnect url:" + ADDRESS_SERVER_URL));
			log.error(name, "DIAMOND-XXXX", "[init-serverlist] fail to get diamond-server serverlist!");
			RuntimeException e = new RuntimeException(
					"fail to get diamond-server serverlist! env:" + name + ", not connnect url:" + ADDRESS_SERVER_URL);
			// �������System.err����쳣ջ
			e.printStackTrace();
			throw e;
		}

        TimerService.scheduleWithFixedDelay(getServersTask, 0L, 30L, TimeUnit.SECONDS);
        isStarted = true;
    }
    
    Iterator<String> iterator() {
        if (serverUrls.isEmpty()) {
            log.error(name, "DIAMOND-XXXX", "[iterator-serverlist] No server address defined!");
        }
        return new ServerAddressIterator(serverUrls);
    }
    

    class GetServerListTask implements Runnable {
        final String url;
        
        GetServerListTask(String url) {
            this.url = url;
        }
        
        @Override
        public void run() {
            // Ĭ�ϣ�local����Ⱥips������ͨ��������������
            if(!StringUtils.isEmpty(DIAMOND_SERVER_IPS) && DEFAULT_NAME.equals(name)) {
                List<String> customIps  = new ArrayList<String>();
                String[] ips = DIAMOND_SERVER_IPS.split(",");
                for(String ip : ips) {
                    customIps.add(ip);
                }
                updateIfChanged(customIps);
                return;
            }

			// get server ips from jmenv
			try {
				updateIfChanged(getApacheServerList(url, name));
			} catch (Throwable e) {
				log.error(name, "DIAMOND-XXXX", "[update-serverlist] failed to update serverlist from address server!",
						e);
			}
        }
    }
    
    private void updateIfChanged(List<String> newList) {
        if (null == newList || newList.isEmpty()) {
        	
        	log.warn("Diamond-0001", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0001", "��������","[update-serverlist] current serverlist from address server is empty!!!"));
            log.warn(name, "[update-serverlist] current serverlist from address server is empty!!!");
            return;
        }
        
        if (newList.equals(serverUrls)) { // no change
            return;
        }       
        serverUrls = new ArrayList<String>(newList);
        //LocalConfigInfoProcessor.cleanEnvSnapshot(name);
        if(env != null && env.agent != null){
		    env.agent.reSetCurrentServerIp();
        }
        
        for (Map.Entry<String, String> ipPort : ipPortMap.entrySet()) {
        	if (!newList.contains(ipPort.getKey())) {
        		ipPortMap.remove(ipPort.getKey());
        	}
        }
        EventDispatcher.fireEvent(new ServerlistChangeEvent());
        log.info(name, "[update-serverlist] serverlist updated to {}", serverUrls);
    }

    // �ӵ�ַ�������õ�ַ�б?����NULL��ʾ�������������ϡ�
    static List<String> getApacheServerList(String url, String name) {
        try {
            HttpResult httpResult = HttpSimpleClient.httpGet(url, null, null, null, 3000);
            
            if (200 == httpResult.code) {
				if (DEFAULT_NAME.equals(name) ) {
					EnvUtil.setSelfEnv(httpResult.headers);
				}
                List<String> lines = IOUtils.readLines(new StringReader(httpResult.content));
                List<String> result = new ArrayList<String>(lines.size());
                for (String line : lines) {
                    if (null == line || line.trim().isEmpty()) {
                        continue;
                    } else {
						String[] ipPort = line.trim().split(":");
						String ip = ipPort[0].trim();
						if (ipPort.length > 1) {
							ipPortMap.put(ip, ipPort[1].trim());
						}
						result.add(ip);
                    }
                }
                return result;
            } else {
                log.error(ADDRESS_SERVER_URL, "DIAMOND-XXXX", "[check-serverlist] error. code={}", httpResult.code);
                return null;
            }
        } catch (IOException e) {
        	log.error("Diamond-0001", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0001", "��������",e.toString()));
            log.error(ADDRESS_SERVER_URL, "DIAMOND-XXXX", "[check-serverlist] exception. msg={}", e.toString(), e);
            return null;
        }
    }
    
    //��ʼ���������б�
    public void initServerList(){    	
        GetServerListTask getServersTask = new GetServerListTask(ADDRESS_SERVER_URL);
        for (int i = 0; i < 3 && serverUrls.isEmpty(); ++i) {
            getServersTask.run();
            try {
                Thread.sleep(100L);
            } catch (Exception e) {
            }
        }
    }
    
    String getUrlString() {
        return serverUrls.toString();
    }

    String getFixedNameSuffix(String... serverIps) {
        StringBuilder sb = new StringBuilder();
        String split = "";
        for (String serverIp : serverIps) {
            sb.append(split);
            sb.append(serverIp);
            split = "-";
        }
        return sb.toString();
    }
    
    public DiamondEnv getEnv() {
		return env;
	}

	public void setEnv(DiamondEnv env) {
		this.env = env;
	}

	@Override
    public String toString() {
        return "ServerManager-" + name + "-" +getUrlString();
    }

	public boolean contain(String ip){
		
		return  serverUrls.contains(ip);
	}

	public String getPortByIp(String ip) {
		String port = StringUtils.defaultIfEmpty(ipPortMap.get(ip), serverPort);
		return port;
	}
	

	
    // ==========================
    String name;// ��ͬ���������
    static public final String DEFAULT_NAME = "default";
    static public final String CUSTOM_NAME = "custom";
    static public final String FIXED_NAME = "fixed";

    // ������server�����ӳ�ʱ��socket��ʱ
    static final int TIMEOUT = 5000;

    final boolean isFixed;
    boolean isStarted = false;
    volatile List<String> serverUrls = new ArrayList<String>();
	private DiamondEnv env = null; //��Ӧ��DiamondEnvʵ��

    //
    public static String serverPort;
    
    static public String ADDRESS_SERVER_URL;

    static public final String DIAMOND_SERVER_IPS;
    
    static public Map<String, String> ipPortMap = new ConcurrentHashMap<String, String>();
	
    static {
		String defaultServerPort = "8080";
		if (TlsUtil.tlsEnable()) {
			defaultServerPort = "443";
		}
        serverPort = System.getProperty("diamond.server.port", defaultServerPort);
        log.info("settings","[req-serv] diamond-server port:{}", serverPort);

        DIAMOND_SERVER_IPS = System.getProperty("DIAMOND.SERVER.IPS","");
        if(!StringUtils.isBlank(DIAMOND_SERVER_IPS)) {
            try {
                String[] ips = DIAMOND_SERVER_IPS.split(",");
                for(String ip : ips) InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                final String msg = "[custom-serverlist] invalid custom server ips:" + DIAMOND_SERVER_IPS;
                log.error("settings", "DIAMOND-XXXX", msg, e);
                throw new IllegalArgumentException(msg, e);
            }
            log.info("settings", "[custom-serverlist] use custom server ips:{}", DIAMOND_SERVER_IPS);
        }
        //TODO �´��ع���ʱ����Ҫ��ɢ������ĸ������ó�ʼ�����ŵ�һ��ȥ
		ADDRESS_SERVER_URL = "http://"+ServerHttpAgent.domainName+":" + ServerHttpAgent.addressPort + "/diamond-server/diamond";
    }

    //
}


/**
 * �Ե�ַ�б�����ͬ�����ȡ� 
 */
class ServerAddressIterator implements Iterator<String> {

    static class RandomizedServerAddress implements Comparable<RandomizedServerAddress> {
        static Random random = new Random();
        
        String serverIp;
        int priority = 0;
        int seed;
        
        public RandomizedServerAddress(String ip) {
            try {
                this.serverIp = ip;
                this.seed = random.nextInt(Integer.MAX_VALUE); //change random scope from 32 to Integer.MAX_VALUE to fix load balance issue
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int compareTo(RandomizedServerAddress other) {
            if (priority != other.priority) {
                return other.priority - priority;
            } else {
                return other.seed - seed;
            }
        }
    }

    public ServerAddressIterator(List<String> source) {
        sorted = new ArrayList<RandomizedServerAddress>();
        for (String address : source) {
            sorted.add(new RandomizedServerAddress(address));
        }
        Collections.sort(sorted);
        iter = sorted.iterator();
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    public String next() {
        return iter.next().serverIp;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    final List<RandomizedServerAddress> sorted;
    final Iterator<RandomizedServerAddress> iter;
}

