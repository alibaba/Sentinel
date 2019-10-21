package com.taobao.diamond.maintenance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.impl.CacheData;
import com.taobao.diamond.client.impl.DiamondEnv;
import com.taobao.diamond.client.impl.HttpSimpleClient;
import com.taobao.diamond.client.impl.LocalConfigInfoProcessor;
import com.taobao.diamond.client.impl.ServerHttpAgent;
import com.taobao.diamond.client.impl.TenantUtil;

/**
 * �ܿؽӿ�
 * 
 * @author Diamond
 *
 */
public class DiamondControl {

	// --------------------------------

	public static String getDomain() {
		return ServerHttpAgent.domainName + ":" + ServerHttpAgent.addressPort;
	}

	// Ip:Port
	public static Map<String, String> getConnect() {
		Map<String, String> env2Server = new HashMap<String, String>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			String currentIp = diamondEnv.getAgent().getCurrentServerIp();
			String port = diamondEnv.getServerMgr().getPortByIp(currentIp);
			String name = diamondEnv.getName();
			env2Server.put(name, currentIp + ":" + port);
		}
		return env2Server;
	}

	// --------------------------------

	public static Map<String, Integer> getListenerSize() {
		Map<String, Integer> env2ListenerSize = new HashMap<String, Integer>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			List<String> listeners = diamondEnv.getAllListeners();
			String envName = diamondEnv.getName();
			env2ListenerSize.put(envName, listeners.size());
		}
		return env2ListenerSize;
	}

	public static Map<String, List<String>> getListeners() {
		Map<String, List<String>> env2Listeners = new HashMap<String, List<String>>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			List<String> listeners = diamondEnv.getAllListeners();
			String envName = diamondEnv.getName();
			env2Listeners.put(envName, listeners);
		}
		return env2Listeners;
	}

	public static Map<String, String> getConfigValueInMemory(String dataId,
			String group) {
		Map<String, String> env2ConfigValueInMemory = new HashMap<String, String>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			CacheData cd = diamondEnv.getCache(dataId, group);
			if (cd != null) {
				String value = cd.getContent();
				if (value != null) {
					env2ConfigValueInMemory.put(diamondEnv.getName(), value);
				}
			}
		}
		return env2ConfigValueInMemory;
	}

	public static Map<String, String> getConfigValueInLocal(String dataId, String group) {
		Map<String, String> env2ConfigValueInLocal = new HashMap<String, String>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			String valueInSnapshot = diamondEnv.getConfigFromSnapshot(TenantUtil.getUserTenant(), dataId, group);
			if (valueInSnapshot != null) {
				env2ConfigValueInLocal.put(diamondEnv.getName(), valueInSnapshot);
			}
		}
		return env2ConfigValueInLocal;
	}

	public static Map<String, String> getConfigValueInServer(String dataId,
			String group) throws IOException {
		Map<String, String> env2ConfigValueInServer = new HashMap<String, String>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			String valueInServer = diamondEnv.getConfig(dataId, group, 3000);
			if (valueInServer != null) {
				env2ConfigValueInServer
						.put(diamondEnv.getName(), valueInServer);
			}
		}
		return env2ConfigValueInServer;
	}

	public static Map<String, String> getMd5InMemory(String dataId, String group) {
		Map<String, String> env2Md5InMemory = new HashMap<String, String>();
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			CacheData cd = diamondEnv.getCache(dataId, group);
			if (cd != null) {
				String md5 = cd.getMd5();
				if (md5 != null) {
					env2Md5InMemory.put(diamondEnv.getName(), md5);
				}
			}
		}
		return env2Md5InMemory;
	}
	
	// --------------------------------

	public static String getSnapshotPath() {
		return LocalConfigInfoProcessor.localSnapShotPath;
	}

	// --------------------------------

	public static String getClientVersion() {
		return HttpSimpleClient.getDIAMOND_CLIENT_VERSION();
	}

	// --------------------------------

	public static String getAppName() {
		return ServerHttpAgent.getAppname();
	}

}
