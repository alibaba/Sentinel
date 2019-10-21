package com.taobao.diamond.mockserver;

import com.taobao.diamond.client.BatchHttpResult;
import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.impl.DiamondEnv;
import com.taobao.diamond.client.impl.DiamondEnvRepo;
import com.taobao.diamond.client.impl.DiamondUnitSite;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.common.GroupKey;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.manager.ManagerListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;


public class MockServer {

    static public ConcurrentHashMap<String, Map<String, String>> serverCacheMap = new ConcurrentHashMap<String, Map<String, String>>();
    static private volatile boolean testMode = false;

    static public void setUpMockServer() {
        testMode = true;
    }

    static public void tearDownMockServer() {
        testMode = false;
    }

    static public boolean isTestMode() {
        return testMode;
    }

    public static String getConfigInfo(String dataId, String group){
        return getConfigInfo(dataId, group, null);
    }

    public static List<ConfigInfoEx> batchQuery(List<String> dataIds, String group){
        return batchQuery(dataIds, group, null);
    }

    public static void setConfigInfos(Map<String/* dataId */, String/* content */> configInfos) {
        setConfigInfos(configInfos, null);
    }

    public static void setConfigInfo(String dataId, String group, String configInfo){
        setConfigInfo(dataId, group, configInfo, null);
    }

    public static void setConfigInfo(String dataId, String configInfo){
        setConfigInfo(dataId, null, configInfo, null);
    }

    public static void removeConfigInfo(String dataId, String group) {
        removeConfigInfo(dataId, group, null);
    }

    // ============================================================================
    public static String getConfigInfo(String dataId, String group, DiamondEnv env){
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        if (env == null) {
            env = DiamondEnvRepo.getDefaultEnv();
        }
        Map<String, String> serverCache = MockServer.serverCacheMap.get(env.getServerMgr().toString());
        return (null != serverCache) ? serverCache.get(GroupKey.getKey(dataId, group)) : null;
    }
    
	public static String getConfigInfo(String dataId, String group, String tenant, DiamondEnv env) {
		if (null == group) {
			group = Constants.DEFAULT_GROUP;
		}
		if (env == null) {
			env = DiamondEnvRepo.getDefaultEnv();
		}
		Map<String, String> serverCache = MockServer.serverCacheMap.get(env.getServerMgr().toString());
		return (null != serverCache) ? serverCache.get(GroupKey.getKeyTenant(dataId, group, tenant)) : null;
	}

    public static List<ConfigInfoEx> batchQuery(List<String> dataIds, String group, DiamondEnv env) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        if (env == null) {
            env = DiamondEnvRepo.getDefaultEnv();
        }
        List<ConfigInfoEx> configInfoExList = new ArrayList<ConfigInfoEx>();
        for (String dataId : dataIds) {
            ConfigInfoEx configInfoEx = new ConfigInfoEx();
            configInfoEx.setDataId(dataId);
            configInfoEx.setGroup(group);

            String config = getConfigInfo(dataId, group, env);

            if(config == null) {
                configInfoEx.setStatus(Constants.BATCH_QUERY_NONEXISTS);
                configInfoEx.setMessage("query data does not exist");
            } else {
                configInfoEx.setContent(config);
                configInfoEx.setStatus(Constants.BATCH_QUERY_EXISTS);
                configInfoEx.setMessage("query success");
            }

            configInfoExList.add(configInfoEx);
        }

        return configInfoExList;
    }

    public static void setConfigInfos(Map<String/* dataId */, String/* content */> configInfos, DiamondEnv env) {
        if (env == null) {
            env = DiamondEnvRepo.getDefaultEnv();
        }
        if (null != configInfos) {
            for (Map.Entry<String, String> entry : configInfos.entrySet()) {
                setConfigInfo(entry.getKey(), entry.getValue(), env);
            }
        }
    }

    public static void setConfigInfo(String dataId, String configInfo, DiamondEnv env) {
        setConfigInfo(dataId, null, configInfo, env);
    }

    public static void setConfigInfo(String dataId, String group, String configInfo, DiamondEnv env) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        if (env == null) {
            env = DiamondEnvRepo.getDefaultEnv();
        }

        Map<String, String> tmp = new ConcurrentHashMap<String, String>();
        Map<String, String> serverCache = serverCacheMap.putIfAbsent(env.getServerMgr().toString(), tmp);
        serverCache = serverCache == null ? tmp : serverCache;

        serverCache.put(GroupKey.getKey(dataId, group), configInfo);
    }

    public static void removeConfigInfo(String dataId, String group, DiamondEnv env) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }

        if (env == null)
            env = DiamondEnvRepo.getDefaultEnv();

        Map<String, String> serverCache = serverCacheMap.get(env.getServerMgr().toString());

        if (serverCache == null) return;

        serverCache.remove(GroupKey.getKey(dataId, group));
    }

    public static void main(String[] args) throws Exception {
        MockServer.setUpMockServer();

        String dataId = "testmockserver";
        String group = "group";

        String newConfig = new Date().toString();
        MockServer.setConfigInfo(dataId, group, newConfig, null);
        //Diamond.publishSingle(dataId, group, newConfig);


        String mockConfig = Diamond.getConfig(dataId, group, 10);
        System.out.println("test-mock-getconfig : " + mockConfig.equals(newConfig));


        MockserverListener listener = new MockserverListener();
        Diamond.addListener(dataId, group, listener);
        while (!newConfig.equals(listener.config)) {
            System.out.println("no receive new config");
            Thread.sleep(1000L);
        }
        System.out.println("test-mock-listener : " +newConfig.equals(listener.config));


        //removeConfigInfo(dataId, group, null);
        Diamond.remove(dataId, group);
        mockConfig = Diamond.getConfig(dataId, group, 10);
        System.out.println(mockConfig == null);

        String dataIdbase = "mockserver.dataid";
        List<String> dataIds = new ArrayList<String>();
        Map<String, String> params = new HashMap<String, String>();
        for(int i = 0; i < 50; i++) {
            //Diamond.publishSingle(dataIdbase+i, null, "mockserver.content"+i);
            DiamondEnv env = DiamondUnitSite.getDiamondUnitEnv("center");
            env.publishSingle(dataIdbase+i, null, "mockserver.content"+i);
            dataIds.add(dataIdbase+i);
            //params.put(dataIdbase+i, "mockserver.content"+ i);
        }

        MockServer.setConfigInfos(params);
        DiamondEnv env = DiamondUnitSite.getDiamondUnitEnv("center");
        BatchHttpResult<ConfigInfoEx> response =  env.batchQuery(dataIds, null, 10l);
        System.out.println(response.isSuccess());
        System.out.println(response.getResult().size());
        for(ConfigInfoEx configInfoEx : response.getResult()) {
            System.out.println(configInfoEx);
        }

    }

    static class MockserverListener implements ManagerListener {
        String config;

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            config = configInfo;
            //System.out.println("receive");
        }
    }

}