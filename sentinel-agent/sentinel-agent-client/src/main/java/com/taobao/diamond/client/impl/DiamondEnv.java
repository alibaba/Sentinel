package com.taobao.diamond.client.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.acm.shaded.com.alibaba.metrics.FastCompass;
import com.taobao.diamond.domain.*;
import com.taobao.diamond.maintenance.DiamondMetric;
import com.taobao.diamond.md5.MD5;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.TypeReference;
import com.taobao.diamond.client.BatchHttpResult;
import com.taobao.diamond.client.impl.HttpSimpleClient.HttpResult;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.common.GroupKey;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.IConfigFilter;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.ConfigFilterChainManager;
import com.taobao.diamond.manager.impl.ConfigRequest;
import com.taobao.diamond.manager.impl.ConfigResponse;
import com.taobao.diamond.mockserver.MockServer;
import com.taobao.diamond.utils.ContentUtils;
import com.taobao.diamond.utils.JSONUtils;
import com.taobao.diamond.utils.ParamUtils;
import com.taobao.diamond.utils.StringUtils;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.support.LoggerHelper;



public class DiamondEnv {

    public void addListeners(String dataId, String group, List<? extends ManagerListener> listeners) {
        group = null2defaultGroup(group);

        CacheData cache = addCacheDataIfAbsent(dataId, group);
        for (ManagerListener listener : listeners) {
            cache.addListener(listener);
        }
    }

    @Deprecated
    public void addListeners(String tenant, String dataId, String group, List<? extends ManagerListener> listeners) throws DiamondException {
    	group = null2defaultGroup(group);
    	ParamUtils.checkTDG(tenant, dataId, group);

    	CacheData cache = addCacheDataIfAbsent(dataId, group, tenant);
    	for (ManagerListener listener : listeners) {
    		cache.addListener(listener);
    	}
    }

	@Deprecated
	public void addTenantListeners(String dataId, String group, List<? extends ManagerListener> listeners) {
		group = null2defaultGroup(group);
		String tenant = TenantUtil.getUserTenant();
		CacheData cache = addCacheDataIfAbsent(dataId, group, tenant);
		for (ManagerListener listener : listeners) {
			cache.addListener(listener);
		}
	}
	
    public void removeListener(String dataId, String group, ManagerListener listener) {
        group = null2defaultGroup(group);

        CacheData cache = getCache(dataId, group);
        if (null != cache) {
            cache.removeListener(listener);
            if (cache.getListeners().isEmpty()) {
                removeCache(dataId, group);
            }
        }
    }

	@Deprecated
	public void removeTenantListener(String dataId, String group, ManagerListener listener) {
		group = null2defaultGroup(group);
		String tenant = TenantUtil.getUserTenant();
		CacheData cache = getCache(dataId, group, tenant);
		if (null != cache) {
			cache.removeListener(listener);
			if (cache.getListeners().isEmpty()) {
				removeCache(dataId, group, tenant);
			}
		}
	}
    
    public void removeListener(String dataId, ManagerListener listener) {
    	String group = null2defaultGroup(null);
    	String tenant = TenantUtil.getUserTenant();
    	CacheData cache = getCache(tenant, dataId, group);
    	if (null != cache) {
    		cache.removeListener(listener);
    		if (cache.getListeners().isEmpty()) {
    			removeCache(tenant, dataId, group);
    		}
    	}
    }

	@Deprecated
    public void removeListener(String tenant, String dataId, String group, ManagerListener listener) throws DiamondException {
    	group = null2defaultGroup(group);
    	ParamUtils.checkTDG(tenant, dataId, group);
    	
    	CacheData cache = getCache(dataId, group, tenant);
    	if (null != cache) {
    		cache.removeListener(listener);
    		if (cache.getListeners().isEmpty()) {
    			removeCache(dataId, group, tenant);
    		}
    	}
    }
    
    public List<ManagerListener> getListeners(String dataId, String group) {
        group = null2defaultGroup(group);
        
        CacheData cache = getCache(dataId, group);
        if (null == cache) {
            return Collections.emptyList();
        }

        return cache.getListeners();
    }

	@Deprecated
	public List<ManagerListener> getListeners(String tenant, String dataId, String group) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkTDG(tenant, dataId, group);

		CacheData cache = getCache(dataId, group, tenant);
		if (null == cache) {
			return Collections.emptyList();
		}

		return cache.getListeners();
	}

	@Deprecated
	public String getTenantConfig(String dataId, String group, long timeoutMs) throws DiamondException {
		return getConfigInner(TenantUtil.getUserTenant(), dataId, group, timeoutMs);
	}

	public List<ConfigKey> getAllTenantConfig(long timeoutMs) throws DiamondException {
		String tenant = TenantUtil.getUserTenant();
    	if (StringUtils.isBlank(tenant)) {
			log.warn("[getAllTenantConfig]", "invalid param tenant=" + tenant);
			throw new DiamondException(400, "invalid param tenant=" + tenant);
		}

		List<ConfigKey> configs = new ArrayList<ConfigKey>();
		Page<ConfigKey> configPage = getAllTenantConfigInner(tenant, 1, 1, timeoutMs);
		int total = configPage.getTotalCount();
		int pageSize = 200;

		for (int i = 0; i * pageSize < total; i++) {
			configPage = getAllTenantConfigInner(tenant, i + 1, pageSize, timeoutMs);
			configs.addAll(configPage.getPageItems());
		}

		return configs;
	}
	
	/**
	 * ���ձ������� -> server -> ���ػ�������ȼ���ȡ���á���ʱ��λ�Ǻ��롣
	 */
	public String getConfig(String dataId, String group, long timeoutMs) throws IOException {
		try {
			return getConfigInner(TenantUtil.getUserTenant(), dataId, group, timeoutMs);
		} catch (DiamondException e) {
			throw new IOException(e.toString());
		}
	}

	@Deprecated
	public String getGlobalConfig(String dataId, String group, long timeoutMs) throws DiamondException {
		return getConfigInner(TenantUtil.getUserTenant(), dataId, group, timeoutMs);
	}
	
	
	/**
	 * ��������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @param timeoutMs ��ȡ��ʱ 
	 * @return ����
	 * @throws DiamondException
	 */
	public String getConfigTag(String dataId, String group, String tag, long timeoutMs) throws DiamondException {
		return getConfigTagInner(TenantUtil.getUserTenant(), dataId, group, tag, timeoutMs);
	}

	@Deprecated
	public String getConfig(String tenant, String dataId, String group, long timeoutMs) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return getConfigInner(tenant, dataId, group, timeoutMs);
	}

	@Deprecated
	public String getConfigTag(String tenant, String dataId, String group, String tag, long timeoutMs)
			throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return getConfigTagInner(tenant, dataId, group, tag, timeoutMs);
	}

	/**
	 * �������֣�Failover�� -> Server -> ���ػ��棨Snapshot��
	 */
	private String getConfigInner(String tenant, String dataId, String group, long timeoutMs) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);
		if (MockServer.isTestMode()) {
			return MockServer.getConfigInfo(dataId, group, this);
		}
		
		// ����ʹ�ñ�������
		String content = LocalConfigInfoProcessor.getFailover(this, dataId, group, tenant);
		if (content != null) {
			log.warn(getName(), "[get-config] get failover ok, dataId={}, group={}, tenant={}, config={}", dataId,
					group, tenant, ContentUtils.truncateContent(content));

			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(this, dataId, group, tenant);
			return doFilter(dataId, group, tenant, content, encryptedDataKey);
		}

		try {
			ConfigInfo configInfo = ClientWorker.getServerConfig(this, dataId, group, tenant, false, timeoutMs);
			return doFilter(dataId, group, tenant, configInfo);
		} catch (DiamondException ioe) {
			if (DiamondException.NO_RIGHT == ioe.getErrCode()) {
				throw ioe;
			}
			log.warn("Diamond-0003",
					LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0003", "��������", "get from server error"));
			log.warn(getName(), "[get-config] get from server error, dataId={}, group={}, tenant={}, msg={}", dataId,
					group, tenant, ioe.toString());
		}

		content = LocalConfigInfoProcessor.getSnapshot(this, dataId, group, tenant);
		log.warn(getName(), "[get-config] get snapshot ok, dataId={}, group={}, tenant={}, config={}", dataId, group,
				tenant, ContentUtils.truncateContent(content));

		String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(this, dataId, group, tenant);
		return doFilter(dataId, group, tenant, content, encryptedDataKey);
	}

	/**
	 * �������֣�Failover�� -> Server
	 */
	private String getServerConfigInner(String tenant, String dataId, String group, long timeoutMs) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);
		if (MockServer.isTestMode()) {
			return MockServer.getConfigInfo(dataId, group, this);
		}

		// ����ʹ�ñ�������
		String content = LocalConfigInfoProcessor.getFailover(this, dataId, group, tenant);
		if (content != null) {
			log.warn(getName(), "[get-config] get failover ok, dataId={}, group={}, tenant={}, config={}", dataId,
					group, tenant, ContentUtils.truncateContent(content));

			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(this, dataId, group, tenant);
			return doFilter(dataId, group, tenant, content, encryptedDataKey);
		}

		try {
			ConfigInfo configInfo = ClientWorker.getServerConfig(this, dataId, group, tenant, false, timeoutMs);
			return doFilter(dataId, group, tenant, configInfo);
		} catch (DiamondException ioe) {
			log.warn("Diamond-0003",
			LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0003", "��������", "get from server error"));
			log.warn(getName(), "[get-config] get from server error, dataId={}, group={}, tenant={}, msg={}", dataId,
					group, tenant, ioe.toString());
			throw ioe;
		}
	}

	private String getConfigTagInner(String tenant, String dataId, String group, String tag, long readTimeout)
			throws DiamondException {
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}

		if (MockServer.isTestMode()) {
			return MockServer.getConfigInfo(dataId, group, this);
		}

		HttpResult result = null;
		try {
			List<String> params = new ArrayList<String>();
			params.add("dataId");
			params.add(dataId);
			params.add("group");
			params.add(group);
			if (StringUtils.isNotEmpty(tenant)) {
				params.add("tenant");
				params.add(tenant);
			}
			if (StringUtils.isNotEmpty(tag)) {
				params.add("tag");
				params.add(tag);
			}

			result = this.agent.httpGet("/config.co", null, params, Constants.ENCODE, readTimeout);
		} catch (IOException e) {
			log.error(getName(), "DIAMOND-XXXX",
					"[sub-server] get server config exception, dataId={}, group={}, tenant={}, msg={}", dataId, group,
					tenant, e.toString());
			throw new DiamondException(DiamondException.SERVER_ERROR, e.getMessage(), e);
		}

		switch (result.code) {
		case HttpURLConnection.HTTP_OK:
			return result.content;
		case HttpURLConnection.HTTP_NOT_FOUND:
			return null;
		case HttpURLConnection.HTTP_CONFLICT: {
			log.error(getName(), "DIAMOND-XXXX",
					"[sub-server-error] get server config being modified concurrently, dataId={}, group={}, tenant={}",
					dataId, group, tenant);
			throw new DiamondException(DiamondException.CONFLICT,
					"data being modified, dataId=" + dataId + ",group=" + group + ",tenant=" + tenant);
		}
		case HttpURLConnection.HTTP_FORBIDDEN: {
			log.error(getName(), "DIAMOND-XXXX", "[sub-server-error] no right, dataId={}, group={}, tenant={}", dataId,
					group, tenant);
			throw new DiamondException(result.code, result.content);
		}
		default: {
			log.error(getName(), "DIAMOND-XXXX", "[sub-server-error]  dataId={}, group={}, tenant={}, code={}", dataId,
					group, tenant, result.code);
			throw new DiamondException(result.code,
					"http error, code=" + result.code + ",dataId=" + dataId + ",group=" + group + ",tenant=" + tenant);
		}
		}
	}

    
    /**
     * ��ȡ��ݽӿڣ��������û�ȡ��ݵ�˳�������������ȣ���<br>
     * feature��������ѡֵ��<br>
     * Constants.GETCONFIG_LOCAL_SERVER_SNAPSHOT(�����ļ�-> ������ -> ���ػ���)<br>
     * Constants.GETCONFIG_LOCAL_SNAPSHOT_SERVER(�����ļ�-> ���ػ��� -> ������)
	 * Constants.GETCONFIG_ONLY_SERVER(�����ļ�-> ������)
     */
    public String getConfig(String dataId, String group, int feature, long timeoutMs) throws IOException{
    	group = null2defaultGroup(group);
    	if(feature == Constants.GETCONFIG_LOCAL_SERVER_SNAPSHOT){
    		return getConfig(dataId, group, timeoutMs);
    	}

    	if(feature == Constants.GETCONFIG_ONLY_SERVER) {
			try {
				return getServerConfigInner(TenantUtil.getUserTenant(), dataId, group, timeoutMs);
			} catch (DiamondException e) {
				throw new IOException(e);
			}
		}

        if (MockServer.isTestMode()) {
            return MockServer.getConfigInfo(dataId, group, this);
        }

        String content = LocalConfigInfoProcessor.getFailover(this, dataId, group, TenantUtil.getUserTenant());
		if (content != null) {
			log.warn(getName(), "[get-config] get failover ok, dataId={}, group={}, tenant={}, config={}", dataId,
					group, TenantUtil.getUserTenant(), ContentUtils.truncateContent(content));

			String tenant = TenantUtil.getUserTenant();
			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(this, dataId, group, tenant);
			try {
				return doFilter(dataId, group, tenant, content, encryptedDataKey);
			} catch (DiamondException e) {
				log.warn(getName(), "[get-config] get failover fail, dataId={}, group={}, tenant={}, config={}", dataId,
					group, TenantUtil.getUserTenant(), ContentUtils.truncateContent(content));
				throw new IOException(e);
			}
		}
		content = LocalConfigInfoProcessor.getSnapshot(this, dataId, group, TenantUtil.getUserTenant());
		if (StringUtils.isNotEmpty(content)) {
			log.warn(getName(), "[get-config] get snapshot ok, dataId={}, group={}, tenant={}, config={}", dataId,
					group, TenantUtil.getUserTenant(), ContentUtils.truncateContent(content));

			String tenant = TenantUtil.getUserTenant();
			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(this, dataId, group, tenant);
			try {
				return doFilter(dataId, group, tenant, content, encryptedDataKey);
			} catch (DiamondException e) {
				log.warn(getName(), "[get-config] get snapshot fail, dataId={}, group={}, tenant={}, config={}", dataId,
					group, TenantUtil.getUserTenant(), ContentUtils.truncateContent(content));
				throw new IOException(e);
			}
		}
		try {
			ConfigInfo configInfo = ClientWorker.getServerConfig(this, dataId, group, timeoutMs);
			return doFilter(dataId, group, TenantUtil.getUserTenant(), configInfo);
		} catch (DiamondException e) {
			throw new IOException(e.toString());
		}
    }

	private String doFilter(String dataId, String group, String tenant, ConfigInfo configInfo)
		throws DiamondException {
		String content = null;
		String encryptedDataKey = null;

		if (configInfo != null) {
			content = configInfo.getContent();
			encryptedDataKey = configInfo.getEncryptedDataKey();
		}

		return doFilter(dataId, group, tenant, content, encryptedDataKey);
	}

	private String doFilter(String dataId, String group, String tenant, String content, String encryptedDataKey)
		throws DiamondException {
		ConfigResponse cr = new ConfigResponse();
		cr.setDataId(dataId);
		cr.setTenant(tenant);
		cr.setGroup(group);
		cr.setContent(content);
		cr.setEncryptedDataKey(encryptedDataKey);

		configFilterChainManager.doFilter(null, cr);

		return cr.getContent();
	}

    public String getConfigFromSnapshot(String tenant, String dataId, String group) {
    	String content = LocalConfigInfoProcessor.getSnapshot(this, dataId, group, tenant);
	    String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(this, dataId, group, tenant);

	    try {
		    return doFilter(dataId, group, tenant, content, encryptedDataKey);
	    } catch (DiamondException e) {
		    log.warn(getName(), "[get-config] get snapshot fail, dataId={}, group={}, tenant={}, config={}", dataId,
			    group, TenantUtil.getUserTenant(), ContentUtils.truncateContent(content));
	    }

	    return content;
    }
    
	public boolean publishSingle(String dataId, String group, String content) {
		return publishSingle(dataId, group, null, content);
	}

	public boolean publishSingleCas(String dataId, String group, String expect, String update) {
		try {
			return publishSingleInnerCas(TenantUtil.getUserTenant(), dataId, group, null, null, null, update, MD5.getInstance().getMD5String(expect));
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}
    
	public boolean publishSingle(String dataId, String group, String appName, String content) {
		try {
			return publishSingleInner(TenantUtil.getUserTenant(), dataId, group, null, appName, null, content);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}

	@Deprecated
	public boolean publishTenantSingle(String dataId, String group, String content) throws DiamondException {
		return publishSingleInner(TenantUtil.getUserTenant(), dataId, group, null, null, null, content);
	}
	
	public boolean publishBeta(String dataId, String group, String betaIps, String content) throws DiamondException {
		ParamUtils.checkBetaIps(betaIps);
		return publishBeta(dataId, group, null, betaIps, content);
	}

	public boolean publishBeta(String dataId, String group, String appName, String betaIps, String content)
			throws DiamondException {
		ParamUtils.checkBetaIps(betaIps);
		return publishSingleInner(TenantUtil.getUserTenant(), dataId, group, null, appName, betaIps, content);
	}

	@Deprecated
	public boolean publishSingle(String tenant, String dataId, String group, String appName, String content)
			throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return publishSingleInner(tenant, dataId, group, null, appName, null, content);
	}

	@Deprecated
	public boolean publishSingleTag(String tenant, String dataId, String group, String tag, String appName,
			String content) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return publishSingleInner(tenant, dataId, group, tag, appName, null, content);
	}

	public boolean publishSingleTag(String dataId, String group, String tag, String appName, String content)
			throws DiamondException {
		return publishSingleInner(TenantUtil.getUserTenant(), dataId, group, tag, appName, null, content);
	}

	/**
	 * ��������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @param content ����
	 * @return �Ƿ����ͳɹ�
	 * @throws DiamondException
	 */
	public boolean publishSingleTag(String dataId, String group, String tag, String content) throws DiamondException {
		return publishSingleInner(TenantUtil.getUserTenant(), dataId, group, tag, null, null, content);
	}
	
	private boolean publishSingleInner(String tenant, String dataId, String group, String tag, String appName, String betaIps,
			String content) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkParam(dataId, group, content);
		if (MockServer.isTestMode()) {
			MockServer.setConfigInfo(dataId, group, content, this);
			return true;
		}
		
		ConfigRequest cr = new ConfigRequest();
		cr.setDataId(dataId);
		cr.setTenant(tenant);
		cr.setGroup(group);
		cr.setContent(content);
		configFilterChainManager.doFilter(cr, null);
		content = cr.getContent();
		
		String url = "/basestone.do?method=syncUpdateAll";
		List<String> params = new ArrayList<String>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		params.add("content");
		params.add(content);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}
		if (StringUtils.isNotEmpty(appName)) {
			params.add("appName");
			params.add(appName);
		}
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tag");
			params.add(tag);
		}
		
		List<String> headers = new ArrayList<String>();
		if (StringUtils.isNotEmpty(betaIps)) {
			headers.add("betaIps");
			headers.add(betaIps);
		}

		String encryptedDataKey = cr.getEncryptedDataKey();
		if (StringUtils.isNotEmpty(encryptedDataKey)) {
			params.add("encryptedDataKey");
			params.add(encryptedDataKey);
		}

		HttpResult result = null;
        FastCompass compass = DiamondMetric.getPublishCompass();
		long start = System.currentTimeMillis();
		long end = 0;
		try {
			result = agent.httpPost(url, headers, params, Constants.ENCODE,
					POST_TIMEOUT);
		} catch (IOException ioe) {
			log.warn("Diamond-0006", LoggerHelper.getErrorCodeStr("Diamond",
					"Diamond-0006", "��������", "[publish-single] exception"));
			log.warn(getName(),
					"[publish-single] exception, dataId={}, group={}, msg={}",
					dataId, group, ioe.toString());

			compass.record(0, "error");
			end = System.currentTimeMillis();
			DiamondMetric.getPublishClusterHistogram().update(end - start);

			return false;
		}

		compass.record(0, "success");
		end = System.currentTimeMillis();
		DiamondMetric.getPublishClusterHistogram().update(end - start);

		if (HttpURLConnection.HTTP_OK == result.code) {
			log.info(getName(), "[publish-single] ok, dataId={}, group={}, tenant={}, config={}", dataId, group, tenant,
					ContentUtils.truncateContent(content));
			return true;
		} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code){
			log.warn(getName(), "[publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId,
					group, tenant, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else {
			log.warn(getName(), "[publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId,
					group, tenant, result.code, result.content);
			return false;
		}
		
	}

	private boolean publishSingleInnerCas(String tenant, String dataId, String group, String tag, String appName, String betaIps,
									   String content, String md5) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkParam(dataId, group, content);
		if (MockServer.isTestMode()) {
			MockServer.setConfigInfo(dataId, group, content, this);
			return true;
		}

		String url = "/basestone.do?method=syncUpdateAll";
		List<String> params = new ArrayList<String>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		params.add("content");
		params.add(content);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}
		if (StringUtils.isNotEmpty(appName)) {
			params.add("appName");
			params.add(appName);
		}
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tag");
			params.add(tag);
		}

		if (StringUtils.isNotEmpty(md5)) {
			params.add("md5");
			params.add(md5);
		}

		List<String> headers = new ArrayList<String>();
		if (StringUtils.isNotEmpty(betaIps)) {
			headers.add("betaIps");
			headers.add(betaIps);
		}



		HttpResult result = null;
		try {
			result = agent.httpPost(url, headers, params, Constants.ENCODE,
					POST_TIMEOUT);
		} catch (IOException ioe) {
			log.warn("Diamond-0006", LoggerHelper.getErrorCodeStr("Diamond",
					"Diamond-0006", "��������", "[publish-single] exception"));
			log.warn(getName(),
					"[publish-single] exception, dataId={}, group={}, msg={}",
					dataId, group, ioe.toString());
			return false;
		}

		if (HttpURLConnection.HTTP_OK == result.code) {
			log.info(getName(), "[publish-single] ok, dataId={}, group={}, tenant={}, config={}", dataId, group, tenant,
					ContentUtils.truncateContent(content));
			return true;
		} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.warn(getName(), "[publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId,
					group, tenant, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else if (HttpURLConnection.HTTP_CONFLICT == result.code){
			log.warn(getName(), "[publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId,
					group, tenant, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else {
			log.warn(getName(), "[publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId,
					group, tenant, result.code, result.content);
			return false;
		}

	}

	
	public boolean publishAggr(String dataId, String group, String datumId, String content) {
		return publishAggr(dataId, group, datumId, null, content);
	}

	public boolean publishAggr(String dataId, String group, String datumId, String appName, String content) {
		try {
			return publishAggrInner(TenantUtil.getUserTenant(), dataId, group, datumId, appName, content);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}

	@Deprecated
	public boolean publishAggr(String tenant, String dataId, String group, String datumId, String appName,
			String content) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return publishAggrInner(tenant, dataId, group, datumId, appName, content);
	}

    private boolean publishAggrInner(String tenant, String dataId, String group, String datumId, String appName, String content) throws DiamondException {
    	group = null2defaultGroup(group);
		ParamUtils.checkParam(dataId, group, datumId, content);
    	String url = "/datum.do?method=addDatum";
		List<String> params = new ArrayList<String>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}
		params.add("datumId");
		params.add(datumId);
		params.add("content");
		params.add(content);

		if (StringUtils.isNotEmpty(appName)) {
			params.add("appName");
			params.add(appName);
		}
    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, POST_TIMEOUT);
    	} catch (IOException ioe) {
			log.warn(getName(), "[publish-aggr] exception, dataId={}, group={}, tenant={}, datumId={}, msg={}", dataId,
					group, tenant, datumId, ioe.toString());
			return false;
    	}
    	
    	if (HttpURLConnection.HTTP_OK == result.code) {
			log.info(getName(), "[publish-aggr] ok, dataId={}, group={}, tenant={}, datumId={}, config={}", dataId,
					group, tenant, datumId, ContentUtils.truncateContent(content));
			return true;
		} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.error(getName(), "[publish-aggr] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId, group,
					tenant, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else {
			log.error(getName(), "[publish-aggr] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId, group,
					tenant, result.code, result.content);
			return false;
    	}
    }
    
	public boolean removeAggr(String dataId, String group, String datumId) {
		try {
			return removeAggrInner(TenantUtil.getUserTenant(), dataId, group, datumId);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}

	@Deprecated
	public boolean removeAggr(String tenant, String dataId, String group, String datumId) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return removeAggrInner(tenant, dataId, group, datumId);
	}
	
    private boolean removeAggrInner(String tenant, String dataId, String group, String datumId) throws DiamondException {
    	checkNotNull(dataId, datumId);
    	group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group, datumId);
    	group = null2defaultGroup(group);
    	String url = "/datum.do?method=deleteDatum";
		List<String> params = new ArrayList<String>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}

		params.add("datumId");
		params.add(datumId);

    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, POST_TIMEOUT);
		} catch (IOException ioe) {
			log.warn(getName(), "[remove-aggr] exception, dataId={}, group={}, tenant={}, datumId={}, msg={}", dataId,
					group, tenant, datumId, ioe.toString());
			return false;
    	}
    	
    	if (HttpURLConnection.HTTP_OK == result.code) {
			log.info(getName(), "[remove-aggr] ok, dataId={}, group={}, tenant={}, datumId={}", dataId, group, tenant,
					datumId);
			return true;
    	} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.error(getName(), "[remove-aggr] error, dataId={}, group={}, tenant={}, datumId={}, code={}, msg={}",
					dataId, group, tenant, datumId, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else {
			log.error(getName(), "[remove-aggr] error, dataId={}, group={}, tenant={}, datumId={}, code={}, msg={}",
					dataId, group, tenant, datumId, result.code, result.content);
			return false;
    	}
    }

	public boolean remove(String dataId, String group) {
		try {
			return removeInner(TenantUtil.getUserTenant(), dataId, group, null);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}

	@Deprecated
	public boolean removeTenantConfig(String dataId, String group) throws DiamondException {
		return removeInner(TenantUtil.getUserTenant(), dataId, group, null);
	}
	
	public boolean remove(String tenant, String dataId, String group) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return removeInner(tenant, dataId, group, null);
	}
	
	/**
	 * ɾ������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @return �Ƿ�ɾ��ɹ�
	 * @throws DiamondException
	 */
	public boolean removeTag(String dataId, String group, String tag) {
		try {
			return removeInner(TenantUtil.getUserTenant(), dataId, group, tag);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return false;
		}
	}

	@Deprecated
	public boolean remove(String tenant, String dataId, String group, String tag) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return removeInner(tenant, dataId, group, tag);
	}

    private boolean removeInner(String tenant, String dataId, String group, String tag) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);
    	
    	if (MockServer.isTestMode()) {
    		MockServer.removeConfigInfo(dataId, group, this);
    		return true;
    	}
    	
    	String url = "/datum.do?method=deleteAllDatums";
    	List<String> params = new ArrayList<String>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tag");
			params.add(tag);
		}
    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, POST_TIMEOUT);
    	} catch (IOException ioe) {
			log.warn("[remove] error, " + dataId + ", " + group + ", " + tenant + ", msg: " + ioe.toString());
			return false;
    	}
    	
    	if (HttpURLConnection.HTTP_OK == result.code) {
			log.info(getName(), "[remove] ok, dataId={}, group={}, tenant={}", dataId, group, tenant);
			return true;
		} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.warn(getName(), "[remove] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId, group,
					tenant, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else {
			log.warn(getName(), "[remove] error, dataId={}, group={}, tenant={}, code={}, msg={}", dataId, group,
					tenant, result.code, result.content);
			return false;
    	}
    }

    public List<String> getServerUrls() {
        return new ArrayList<String>(serverMgr.serverUrls);
    }
    
    private static void checkNotNull(String... params) {
        for (String param : params) {
            if (StringUtils.isBlank(param)) {
                throw new IllegalArgumentException("param cannot be blank");
            }
        }
    }
    

	
    
    private String null2defaultGroup(String group) {
        return (null == group) ? Constants.DEFAULT_GROUP : group.trim();
    }

    /**
     * ������ѯ���õ�server snapshotֵ
     */
    public BatchHttpResult<ConfigInfoEx> batchGetConfig(List<String> dataIds, String group, long timeoutMs) {
        // check parameters
        if (dataIds == null) {
            throw new IllegalArgumentException("dataId list is null when batch get config");
        }

        group = null2defaultGroup(group);

		try {
			return batchGetConfigInner(TenantUtil.getUserTenant(), dataIds, group, timeoutMs);
		} catch (DiamondException e) {
			if (e.getErrCode() == DiamondException.CLIENT_INVALID_PARAM) {
				throw new IllegalArgumentException(e.toString());
			}
			if (e.getErrCode() == DiamondException.NO_RIGHT) {
				throw new AccessControlException(e.toString());
			}
			return new BatchHttpResult<ConfigInfoEx>(false, -1, "batch get config exception:" + e.toString(), "");
		}
    }

    @Deprecated
	public BatchHttpResult<ConfigInfoEx> batchGetConfig(String tenant, List<String> dataIds, String group,
			long timeoutMs) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return batchGetConfigInner(tenant, dataIds, group, timeoutMs);
	}
    
    private BatchHttpResult<ConfigInfoEx> batchGetConfigInner(String tenant, List<String> dataIds, String group, long timeoutMs) throws DiamondException {
		group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataIds, group);
		// check parameters
		if (dataIds == null) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM,
					"dataId list is null when batch get config");
		}
    	
    	if(MockServer.isTestMode()){
    		List<ConfigInfoEx> result = MockServer.batchQuery(dataIds, group, this);
    		BatchHttpResult<ConfigInfoEx> response = new BatchHttpResult<ConfigInfoEx>(true, HttpURLConnection.HTTP_OK, "", "mock server");
    		response.getResult().addAll(result);
    		return response;
    	}
    	
    	StringBuilder dataIdstr = new StringBuilder();
    	String split = "";
    	for (String dataId : dataIds) {
    		dataIdstr.append(split);
    		dataIdstr.append(dataId);
    		split = Constants.WORD_SEPARATOR;
    	}
    	
    	// fire http request
		String url = "/config.co?method=batchGetConfig";
		List<String> params = new ArrayList<String>();
		params.add("dataIds");
		params.add(dataIdstr.toString());
		params.add("group");
		params.add(group);
		if (StringUtils.isNotEmpty(tenant)) {
			params.add("tenant");
			params.add(tenant);
		}
    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, timeoutMs);
    	} catch (IOException ioe) { // ��������ʧ��
    		log.warn(getName(), "[batch-get] exception, dataIds={}, group={}, tenant={}, msg={}", dataIds, group, tenant, ioe);
    		return new BatchHttpResult<ConfigInfoEx>(false, -1, "batch get config io exception:" + ioe.getMessage(), "");
    	}
    	
    	// prepare response
    	BatchHttpResult<ConfigInfoEx> response = new BatchHttpResult<ConfigInfoEx>(true, result.code, "", result.content);
    	
    	// handle http code
    	if(result.code == HttpURLConnection.HTTP_OK){ // http code 200
    		response.setSuccess(true);
    		response.setStatusMsg("batch get config success");
    		log.info(getName(), "[batch-get] ok, dataIds={}, group={}, tenant={}", dataIds, group, tenant);
    	} else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.warn(getName(), "[batch-get] error, dataIds={}, group={}, tenant={}, code={}, msg={}", dataIds, group, tenant,
					result.code, result.content);
			throw new DiamondException(result.code, result.content);
		} else { // http code: 412 500
    		response.setSuccess(false);
    		response.setStatusMsg("batch get config fail, status:" + result.code);
			log.warn(getName(), "[batch-get] error, dataIds={}, group={}, tenant={}, code={}, msg={}", dataIds, group,
					tenant, result.code, result.content);
		}
    	
    	// deserialize batch query result items
    	if (HttpURLConnection.HTTP_OK == result.code ||
    			HttpURLConnection.HTTP_PRECON_FAILED == result.code) {
    		try {
    			String json = result.content;
    			Object resultObj = JSONUtils.deserializeObject(json,
    					new TypeReference<List<ConfigInfoEx>>() {
    			});
    			response.getResult().addAll((List<ConfigInfoEx>) resultObj);
//                long t1 = System.currentTimeMillis();
    			LocalConfigInfoProcessor.batchSaveSnapshot(this, (List<ConfigInfoEx>)resultObj);
			    LocalEncryptedDataKeyProcessor.batchSaveEncryptDataKeySnapshot(this, (List<ConfigInfoEx>)resultObj);
//                long t2 = System.currentTimeMillis();
//                log.info(getName(), "[batch-get] save snapshots cost"+ (t2-t1) +"ms"+ " file cnt="+((List<ConfigInfoEx>)resultObj).size()); 
    			
    		} catch (Exception e) {  // ������Ӧʧ��
    			response.setSuccess(false);
    			response.setStatusMsg("batch get config deserialize error");
    			log.warn(getName(), "[batch-get] deserialize error, dataIds={}, group={}, tenant={}, msg={}", dataIds, group, tenant, e.toString());
    		}
    	}
    	
    	return response;
    }

	private Page<ConfigKey> getAllTenantConfigInner(String tenant, int pageNo, int pageSize, long timeoutMs) throws DiamondException {
		ParamUtils.checkTenant(tenant);

		// fire http request
		String url = "/basestone.do";
		List<String> params = new ArrayList<String>();
		params.add("method");
		params.add("getAllConfigByTenant");
		params.add("tenant");
		params.add(tenant);
		params.add("pageNo");
		params.add(pageNo + "");
		params.add("pageSize");
		params.add(pageSize + "");
		HttpResult result = null;

		try {
			result = agent.httpGet(url, null, params, Constants.ENCODE, timeoutMs);
		} catch (IOException e) {
			log.warn(getName(), "[getAllTenantConfig] get config IOException, tenant={}", tenant);
			throw new DiamondException(result.code, "get config IOException tenant=" + tenant, e);
		}
		// handle http code
		if(result.code == HttpURLConnection.HTTP_OK){ // http code 200
			log.info(getName(), "[getAllTenantConfig] ok, tenant={}", tenant);
		} else {
			log.warn(getName(), "[getAllTenantConfig] error, tenant={}, code={}, msg={}", tenant, result.code, result.content);
			throw new DiamondException(result.code, "getConfigInfoByTenant error tenant=" + tenant + " " + result.content);
		}

		// deserialize result
		try {
			return (Page<ConfigKey>) JSONUtils.deserializeObject(result.content, new TypeReference<Page<ConfigKey>>() {});
		} catch (Exception e) {  // ������Ӧʧ��
			log.warn(getName(), "[getAllTenantConfig] error, tenant={}, code={}, msg={}", tenant, result.code, result.content);
			throw new DiamondException(result.code, "getAllTenantConfig error tenant=" + tenant, e);
		}
	}


	/**
     * ������ѯ���õ�dbֵ
     */
    @SuppressWarnings("unchecked")
    public BatchHttpResult<ConfigInfoEx> batchQuery(List<String> dataIds, String group,
            long timeoutMs) {

        // �������ؽ��
        BatchHttpResult<ConfigInfoEx> response = new BatchHttpResult<ConfigInfoEx>();

        // �ж�list�Ƿ�Ϊnull
        if (dataIds == null) {
            throw new IllegalArgumentException("dataId list is null when batch query");
        }

        group = null2defaultGroup(group);

        if(MockServer.isTestMode()){
            List<ConfigInfoEx> result = MockServer.batchQuery(dataIds, group, this);
            response.setStatusCode(HttpURLConnection.HTTP_OK);
            response.setResponseMsg("mock server");
            response.setSuccess(true);
            response.getResult().addAll(result);
            return response;
        }

        // ��dataId��list����Ϊ��һ�����ɼ��ַ�ָ����ַ�
        StringBuilder dataIdstr = new StringBuilder();
        String split = "";
        for (String dataId : dataIds) {
            dataIdstr.append(split);
            dataIdstr.append(dataId);
            split = Constants.WORD_SEPARATOR;
        }

        String url = "/admin.do?method=batchQuery";
        List<String> params = Arrays.asList("dataIds", dataIdstr.toString(), "group", group);

        HttpResult result = null;
        try {
            result = agent.httpPost(url, null, params, Constants.ENCODE, timeoutMs);
        } catch (IOException ioe) {
            log.warn(getName(), "[batch-query] exception, dataIds={}, group={}, msg={}", dataIds, group, ioe);
            response.setSuccess(false);
            response.setStatusMsg("batch query io exception��" + ioe.getMessage());
            return response;
        }

        response.setStatusCode(result.code);
        response.setResponseMsg(result.content);

        // error result code
        if (HttpURLConnection.HTTP_OK == result.code ||
                HttpURLConnection.HTTP_PRECON_FAILED == result.code) {

            try {
                String json = result.content;
                Object resultObj = JSONUtils.deserializeObject(json,
                        new TypeReference<List<ConfigInfoEx>>() {
                        });
                response.setSuccess(true);
                response.getResult().addAll((List<ConfigInfoEx>) resultObj);
                log.info(getName(), "[batch-query] ok, dataIds={}, group={}", dataIds, group);
            } catch (Exception e) {
                response.setSuccess(false);
                response.setStatusMsg("batch query deserialize error");
                log.warn(getName(), "[batch-query] deserialize error, dataIds={}, group={}, msg={}", dataIds, group, e.toString());
            }


        } else {
            response.setSuccess(false);
            response.setStatusMsg("batch query fail, status:" + result.code);
            log.warn(getName(), "[batch-query] error, dataIds={}, group={}, code={}, msg={}", dataIds, group, result.code, result.content);
            return response;

        }

        return response;
    }
    
	public boolean stopBeta(String dataId, String group) throws DiamondException {
		return stopBetaInner(TenantUtil.getUserTenant(), dataId, group);
	}

	@Deprecated
	public boolean stopBeta(String tenant, String dataId, String group) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return stopBetaInner(tenant, dataId, group);
	}

	private boolean stopBetaInner(String tenant, String dataId, String group) throws DiamondException {
		// �������ؽ��
		boolean response = false;
		group = null2defaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);

		String url = "/admin.do";
		List<String> params = null;
		if (StringUtils.isBlank(tenant)) {
			params = Arrays.asList("method", "stopBeta", "dataId", dataId, "group", group);
		} else {
			params = Arrays.asList("method", "stopBeta", "tenant", tenant, "dataId", dataId, "group", group);
		}
		
		HttpResult result = null;
		try {
			result = agent.httpGet(url, null, params, Constants.ENCODE, 3000);
		} catch (IOException ioe) {
			log.warn(getName(), "[stopBeta] exception, tenant={}, dataId={}, group={}, msg={}", tenant, dataId, group,
					ioe);
			throw new DiamondException(DiamondException.SERVER_ERROR, ioe.getMessage(), ioe);
		}

		// error result code
		if (HttpURLConnection.HTTP_OK == result.code) {
			try {
				String json = result.content;
				Object resultObj = JSONUtils.deserializeObject(json, new TypeReference<RestResult<Boolean>>() {
				});
				RestResult<Boolean> tmp = (RestResult<Boolean>) resultObj;
				if (200 != tmp.getCode()) {
					log.warn(getName(), "[stopBeta] error, tenant={}, dataId={}, group={}, code={}, msg={}", tenant,
							dataId, group, result.code, result.content);
					throw new DiamondException(tmp.getCode(), tmp.getMessage());
				} else {
					response = tmp.getData();
					log.info(getName(), "[stopBeta] ok, tenant={}, dataId={}, group={}", tenant, dataId, group);
				}
			} catch (Exception e) {
				log.warn(getName(), "[stopBeta] deserialize error, tenant={}, dataId={} group={}, msg={}", tenant,
						dataId, group, e.toString());
				throw new DiamondException(DiamondException.SERVER_ERROR, e.getMessage(), e);
			}
		} else {
			log.warn(getName(), "[stopBeta] error, tenant={}, dataId={}, group={}, code={}, msg={}", tenant, dataId,
					group, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		}
		return response;
	}
    
	public ConfigInfo4Beta getBeta(String dataId, String group) throws DiamondException {
		return getBetaInner(TenantUtil.getUserTenant(), dataId, group);
	}

	@Deprecated
	public ConfigInfo4Beta getBeta(String tenant, String dataId, String group) throws DiamondException {
		ParamUtils.checkTenant(tenant);
		return getBetaInner(tenant, dataId, group);
	}
    
    private ConfigInfo4Beta getBetaInner(String tenant, String dataId, String group) throws DiamondException {
    	// �������ؽ��
    	ConfigInfo4Beta response = null;
    	group = null2defaultGroup(group);
    	
    	ParamUtils.checkKeyParam(dataId, group);

		String url = "/admin.do";
		
		List<String> params = null;
		if (StringUtils.isBlank(tenant)) {
			params = Arrays.asList("method", "queryBeta", "dataId", dataId, "group", group);
		} else {
			params = Arrays.asList("method", "queryBeta", "tenant", tenant, "dataId", dataId, "group", group);
		}

		HttpResult result = null;
		try {
			result = agent.httpGet(url, null, params, Constants.ENCODE_UTF_8, 3000);
		} catch (IOException ioe) {
			throw new DiamondException(DiamondException.SERVER_ERROR, ioe.getMessage(), ioe);
		}
    	
    	// error result code
		if (HttpURLConnection.HTTP_OK == result.code) {
			try {
				String json = result.content;
				Object resultObj = JSONUtils.deserializeObject(json, new TypeReference<RestResult<ConfigInfo4Beta>>() {
				});
				RestResult<ConfigInfo4Beta> tmp = (RestResult<ConfigInfo4Beta>) resultObj;
				if (200 != tmp.getCode()) {
					log.warn(getName(), "[getBeta] error, tenant={}, dataId={}, group={}, code={}, msg={}", tenant,
							dataId, group, result.code, result.content);
					throw new DiamondException(tmp.getCode(), tmp.getMessage());
				} else {
					response = tmp.getData();
					log.info(getName(), "[getBeta] ok, tenant={}, dataId={}, group={}", tenant, dataId, group);
				}
			} catch (IOException e) {
				log.warn(getName(), "[getBeta] deserialize error, tenant={}, dataId={} group={}, msg={}", tenant,
						dataId, group, e.toString());
				throw new DiamondException(DiamondException.SERVER_ERROR, e.getMessage(), e);
			}
		} else {
			log.warn(getName(), "[getBeta] error, tenant={}, dataId={}, group={}, code={}, msg={}", tenant, dataId,
					group, result.code, result.content);
			throw new DiamondException(result.code, result.content);
		}
		return response;
    }
    
    /**
     * ����ɾ��ۺ����
     * @param dataId
     * @param group
     * @param datumIdList
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public boolean batchRemoveAggr(String dataId, String group, List<String> datumIdList, long timeoutMs) {
    	checkNotNull(dataId, group);
    	if(datumIdList == null || datumIdList.isEmpty()){
    		throw new IllegalArgumentException("datumIdList cannot be blank"); 
    	}
    	StringBuilder datumStr = new StringBuilder();
    	for(String datum : datumIdList){
    		datumStr.append(datum).append(Constants.WORD_SEPARATOR);
    	}
    	String url = "/datum.do?method=batchDeleteAggrs";
    	List<String> params = Arrays.asList("dataId", dataId, "group", group, "datumList", datumStr.toString());
    	HttpResult result = null;
        try {
            result = agent.httpPost(url, null, params, Constants.ENCODE, timeoutMs);
            if(result.code == HttpURLConnection.HTTP_OK){
            	return true;
            } else {
                log.warn("response code :"+result.code + ", error message :" + result.content);
            }
        } catch (IOException ioe) {
            log.warn(getName(), "[batchRemoveAggr] exception, dataId{}, group={}, msg={}", dataId, group, ioe);
        }
        return false;
    }
    
    /**
     * �����ķ������߸��¾ۺ����
     * @param dataId
     * @param group
     * @param datumMap
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public boolean batchPublishAggr(String dataId, String group, Map<String, String> datumMap, long timeoutMs){
    	return batchPublishAggr(dataId, group, datumMap, null, timeoutMs);
    }
    
    /**
     * �����ķ������߸��¾ۺ����
     * @param dataId
     * @param group
     * @param datumMap
     * @param appName ���ù�����app name
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public boolean batchPublishAggr(String dataId, String group, Map<String, String> datumMap, String appName, long timeoutMs){
    	checkNotNull(dataId, group);
    	if(datumMap == null || datumMap.isEmpty()){
    		throw new IllegalArgumentException("datumMap cannot be blank"); 
    	}
    	StringBuilder datumStr = new StringBuilder();
    	for(Entry<String, String> datumEntry : datumMap.entrySet()){
    		datumStr.append(datumEntry.getKey()).append(Constants.WORD_SEPARATOR).append(datumEntry.getValue()).append(Constants.LINE_SEPARATOR);
    	}
		String url = "/datum.do?method=batchAddAggrs";
		List<String> params = null;
		if (appName == null) {
			params = Arrays.asList("dataId", dataId, "group", group, "datas",
					datumStr.toString());
		} else {
			params = Arrays.asList("dataId", dataId, "group", group, "datas",
					datumStr.toString(), "appName", appName);
		}
    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, timeoutMs);
    		if(result.code == HttpURLConnection.HTTP_OK){
    			log.info(getName(),
    					"[batchPublishAggr] ok, dataId={}, group={}", dataId,
    					group);
    			return true;
    		} else {
    			log.warn("response code :"+result.code + ", error message :" + result.content);
    		}
    	} catch (IOException ioe) {
    		log.warn(getName(), "[batchPublishAggr] exception, dataId{}, group={}, msg={}", dataId, group, ioe);
    	}
    	return false;
    }
    
    /**
     * ���ո��datum�б?�滻dataid+group�����е����
     * @param dataId
     * @param group
     * @param datumMap
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public boolean replaceAggr(String dataId, String group, Map<String, String> datumMap, long timeoutMs) {
    	return replaceAggr(dataId, group, datumMap, null, timeoutMs);
    }
    
    /**
     * ���ո��datum�б?�滻dataid+group�����е����
     * @param dataId
     * @param group
     * @param datumMap
     * @param appName ���ù�����app name
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public boolean replaceAggr(String dataId, String group, Map<String, String> datumMap, String appName, long timeoutMs) {
    	checkNotNull(dataId, group);
    	if(datumMap == null || datumMap.isEmpty()){
    		throw new IllegalArgumentException("datumMap cannot be blank"); 
    	}
    	StringBuilder datumStr = new StringBuilder();
    	for(Entry<String, String> datumEntry : datumMap.entrySet()){
    		datumStr.append(datumEntry.getKey()).append(Constants.WORD_SEPARATOR).append(datumEntry.getValue()).append(Constants.LINE_SEPARATOR);
    	}
    	String url = "/datum.do?method=replaceAggr";
    	List<String> params  = null;
    	if (appName == null) {
    		params = Arrays.asList("dataId", dataId, "group", group, "datas", datumStr.toString());
		} else {
			params = Arrays.asList("dataId", dataId, "group", group, "datas",
					datumStr.toString(), "appName", appName);
		}
    	
    	HttpResult result = null;
    	try {
    		result = agent.httpPost(url, null, params, Constants.ENCODE, timeoutMs);
    		if(result.code == HttpURLConnection.HTTP_OK){
    			return true;
    		} else{
    			log.warn("response code :"+result.code + ", error message :" + result.content);
    		}
    	} catch (IOException ioe) {
    		log.warn(getName(), "[replaceAggr] exception, dataId{}, group={}, msg={}", dataId, group, ioe);
    	}
    	return false;
    }
    
    /**
     * ��ѯCacheData������NULL��ʾ�Ҳ�����
     */
	public CacheData getCache(String dataId, String group) {
		return getCache(dataId, group, TenantUtil.getUserTenant());
	}
    
	public CacheData getCache(String dataId, String group, String tenant) {
		if (null == dataId || null == group) {
			throw new IllegalArgumentException();
		}
		return cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
	}
    
    List<CacheData> getAllCacheDataSnapshot() {
        return new ArrayList<CacheData>(cacheMap.get().values());
    }
    
	public int getAllCacheDataSize() {
		return cacheMap.get().size();
	}
    
	public List<String> getAllListeners() {
		return new ArrayList<String>(cacheMap.get().keySet());
	}
    
    void removeCache(String dataId, String group) {
        String groupKey = GroupKey.getKeyTenant(dataId, group, TenantUtil.getUserTenant());
        synchronized (cacheMap) {
            Map<String, CacheData> copy = new HashMap<String, CacheData>(cacheMap.get());
            copy.remove(groupKey);
            cacheMap.set(copy);
        }
        log.info(getName(), "[unsubscribe] {}", groupKey);
    }
    
    void removeCache(String dataId, String group, String tenant) {
    	String groupKey = GroupKey.getKeyTenant(dataId, group, tenant);
    	synchronized (cacheMap) {
    		Map<String, CacheData> copy = new HashMap<String, CacheData>(cacheMap.get());
    		copy.remove(groupKey);
    		cacheMap.set(copy);
    	}
    	log.info(getName(), "[unsubscribe] {}", groupKey);
    }
    
    /**
     * ��ѯCacheData��������ʱ������
     */
    public CacheData addCacheDataIfAbsent(String dataId, String group) {
        CacheData cache = getCache(dataId, group);
        if (null != cache) {
            return cache;
        }

        String key = GroupKey.getKeyTenant(dataId, group, TenantUtil.getUserTenant());
        cache = new CacheData(this, dataId, group);

        synchronized (cacheMap) {
        	CacheData cacheFromMap = getCache(dataId, group);
        	// multiple listeners on the same dataid+group and race condition,so double check again
        	if(null != cacheFromMap) { //other listener thread beat me to set to cacheMap
        		cache = cacheFromMap;
        		cache.setInitializing(true); //reset so that server not hang this check
			} else {
				int taskId = getAllCacheDataSize() / (int) getPER_TASK_CONFIG_SIZE();
				cache.setTaskId(taskId);
			}
        	
            Map<String, CacheData> copy = new HashMap<String, CacheData>(cacheMap.get());
            copy.put(key, cache);
            cacheMap.set(copy);
        }
        
        log.info(getName(), "[subscribe] {}", key);
        
        return cache;
    }
    /**
     * ��ѯCacheData��������ʱ������
     * @throws DiamondException 
     */
    public CacheData addCacheDataIfAbsent(String dataId, String group, String tenant) {
    	
		CacheData cache = getCache(dataId, group, tenant);
		if (null != cache) {
			return cache;
		}

		String key = GroupKey.getKeyTenant(dataId, group, tenant);
		cache = new CacheData(this, dataId, group, tenant);
    	
    	synchronized (cacheMap) {
    		CacheData cacheFromMap = getCache(dataId, group, tenant);
    		// multiple listeners on the same dataid+group and race condition,so double check again
    		if(null != cacheFromMap) { //other listener thread beat me to set to cacheMap
    			cache = cacheFromMap;
    			cache.setInitializing(true); //reset so that server not hang this check
    		}
    		
    		Map<String, CacheData> copy = new HashMap<String, CacheData>(cacheMap.get());
    		copy.put(key, cache);
    		cacheMap.set(copy);
    	}
    	
    	log.info(getName(), "[subscribe] {}", key);
    	
    	return cache;
    }
    
    /**
     * �������ж���dataId��ֻ�����ϡ�
     */
    public Set<String> getSubscribeDataIds() {
        Map<String, CacheData> cacheMapSnapshot = cacheMap.get();
        
        Set<String> dataIds = new HashSet<String>(cacheMapSnapshot.size());
        for (CacheData cache : cacheMapSnapshot.values()) {
            dataIds.add(cache.dataId);
        }
        return dataIds;
    }
    
    
    @Override
    public String toString() {
        return "DiamondEnv-" + serverMgr.toString();
    }

    public ServerListManager getServerMgr() {
        return serverMgr;
    }

    public String getName() {
        return serverMgr.name;
    }

    public ServerHttpAgent getAgent() {
		return agent;
	}
    
    public ClientWorker getWorker() {
		return worker;
	}
    
    public void initServerManager(ServerListManager _serverMgr) {
    	_serverMgr.setEnv(this);
        serverMgr = _serverMgr;
        serverMgr.start();
        agent = new ServerHttpAgent(serverMgr);
    }
    
    public double getPER_TASK_CONFIG_SIZE() {
		return PER_TASK_CONFIG_SIZE;
	}
    
	public void setPER_TASK_CONFIG_SIZE(double pER_TASK_CONFIG_SIZE) {
		PER_TASK_CONFIG_SIZE = pER_TASK_CONFIG_SIZE;
	}

    public DiamondEnv(String... serverIps) {
        this(new ServerListManager(Arrays.asList(serverIps)));
    }
    
	public void addConfigFilter(IConfigFilter configFilter) {
		configFilterChainManager.addFilter(configFilter);
	}
	
	public ConfigFilterChainManager getConfigFilterChainManager() {
		return configFilterChainManager;
	}
    
    protected DiamondEnv(ServerListManager serverListMgr) {
		serverListMgr.setEnv(this);
		try {
			PER_TASK_CONFIG_SIZE = Double.valueOf(System.getProperty("PER_TASK_CONFIG_SIZE", "3000"));
			log.warn("PER_TASK_CONFIG_SIZE:", PER_TASK_CONFIG_SIZE);
		} catch (Throwable t) {
			log.error("PER_TASK_CONFIG_SIZE", "PER_TASK_CONFIG_SIZE invalid", t);
		}
        initServerManager(serverListMgr);
        cacheMap = new AtomicReference<Map<String, CacheData>>(new HashMap<String, CacheData>());
        worker = new ClientWorker(this);
    }
    
    // =====================

    static final public Logger log = LogUtils.logger(DiamondEnv.class);
    static public final long POST_TIMEOUT = 3000L;
        
    protected ServerListManager serverMgr;
    protected ServerHttpAgent agent; // �����server��ϵ
    protected ClientWorker worker ;
    
    final private AtomicReference<Map<String/* groupKey */, CacheData>> cacheMap; // COW����
	private double PER_TASK_CONFIG_SIZE = 3000;
	
	private ConfigFilterChainManager configFilterChainManager = new ConfigFilterChainManager();


}
