package com.taobao.diamond.client.impl;

import com.alibaba.acm.shaded.com.alibaba.metrics.FastCompass;
import com.taobao.diamond.client.impl.HttpSimpleClient.HttpResult;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.common.GroupKey;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.maintenance.DiamondMetric;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.mockserver.MockServer;
import com.taobao.diamond.utils.ContentUtils;
import com.taobao.diamond.utils.StringUtils;
import com.taobao.middleware.logger.support.LoggerHelper;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.taobao.diamond.client.impl.DiamondEnv.log;
import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

public class ClientWorker {

	/**
	 * ����404��Ӧ�룬����NULL.
	 * 
	 * @throws IOException
	 */
	static ConfigInfo getServerConfig(DiamondEnv env, String dataId, String group, long readTimeout) throws DiamondException {
		return getServerConfig(env, dataId, group, TenantUtil.getUserTenant(), false, readTimeout);
	}

	/**
	 * ����404��Ӧ�룬����NULL.
	 * 
	 * @throws IOException
	 */
	static ConfigInfo getServerConfig(DiamondEnv env, String dataId, String group, String tenant, boolean notify, long readTimeout) throws DiamondException {
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}

		if (MockServer.isTestMode()) {
			ConfigInfo configInfo = new ConfigInfo();
			configInfo.setContent(MockServer.getConfigInfo(dataId, group, env));
			return configInfo;
		}

		HttpResult result = null;
		FastCompass compass = DiamondMetric.getConfigCompass();
		long start = System.currentTimeMillis();
		long end = 0;

		try {
			List<String> params = null;
			if (StringUtils.isBlank(tenant)) {
				params = Arrays.asList("dataId", dataId, "group", group);
			} else {
				params = Arrays.asList("dataId", dataId, "group", group, "tenant", tenant);
			}

			List<String> headers = null;
			if (notify) {
				headers = Arrays.asList("notify", String.valueOf(notify));
			}

			result = env.agent.httpGet("/config.co", headers, params, Constants.ENCODE, readTimeout);
		} catch (IOException e) {
			log.error(env.getName(), "DIAMOND-XXXX",
					"[sub-server] get server config exception, dataId={}, group={}, tenant={}, msg={}", dataId, group,
					tenant, e.toString());
			compass.record(0, "error");
			end = System.currentTimeMillis();
			DiamondMetric.getClusterHistogram().update(end - start);
			throw new DiamondException(DiamondException.SERVER_ERROR, e.getMessage(), e);
		}
		compass.record(0, "success");
		end = System.currentTimeMillis();
		DiamondMetric.getClusterHistogram().update(end - start);
		switch (result.code) {
		case HttpURLConnection.HTTP_OK:
		    ConfigInfo configInfo = new ConfigInfo();
            configInfo.setDataId(dataId);
            configInfo.setGroup(group);
            configInfo.setTenant(tenant);
            configInfo.setContent(result.content);

			setEncryptedDataKey(configInfo, result);

			LocalConfigInfoProcessor.saveSnapshot(env, dataId, group, tenant, result.content);
			LocalEncryptedDataKeyProcessor.saveEncryptDataKeySnapshot(env, dataId, group, tenant, configInfo.getEncryptedDataKey());

			return configInfo;
		case HttpURLConnection.HTTP_NOT_FOUND:
			LocalConfigInfoProcessor.saveSnapshot(env, dataId, group, tenant, null);
			LocalEncryptedDataKeyProcessor.saveEncryptDataKeySnapshot(env, dataId, group, tenant, null);
			return null;
		case HttpURLConnection.HTTP_CONFLICT: {
			log.error(env.getName(), "DIAMOND-XXXX",
					"[sub-server-error] get server config being modified concurrently, dataId={}, group={}, tenant={}", dataId,
					group, tenant);
			throw new DiamondException(DiamondException.CONFLICT,
					"data being modified, dataId=" + dataId + ",group=" + group + ",tenant=" + tenant);
		}
		case HttpURLConnection.HTTP_FORBIDDEN: {
			log.error(env.getName(), "DIAMOND-XXXX", "[sub-server-error] no right, dataId={}, group={}, tenant={}",
					dataId, group, tenant);
			throw new DiamondException(result.code, result.content);
		}
		default: {
			log.error(env.getName(), "DIAMOND-XXXX", "[sub-server-error]  dataId={}, group={}, tenant={}, code={}",
					dataId, group, tenant, result.code);
			throw new DiamondException(result.code,
					"http error, code=" + result.code + ",dataId=" + dataId + ",group=" + group + ",tenant=" + tenant);
		}
		}
	}

	private static void setEncryptedDataKey(ConfigInfo configInfo, HttpResult result) {
		Map<String, List<String>> headers = result.headers;
		if (headers != null && !headers.isEmpty()) {
			List<String> list = headers.get("Encrypted-Data-Key");
			if (list != null && !list.isEmpty()) {
				configInfo.setEncryptedDataKey(list.get(0));
			}
		}
	}

	/**
	 * ��鱾�������ļ�����ʱ�޸��Ƿ�ʹ�ñ������ֱ�־λ����������ʱ�����md5.
	 */
	static void checkLocalConfig(DiamondEnv env, CacheData cacheData) {
		final String dataId = cacheData.dataId;
		final String group = cacheData.group;
		final String tenant = cacheData.tenant;
		File path = LocalConfigInfoProcessor.getFailoverFile(env, dataId, group, tenant);

		// û�� -> ��
		if (!cacheData.isUseLocalConfigInfo() && path.exists()) {
			String content = LocalConfigInfoProcessor.getFailover(env, dataId, group, tenant);
			String md5 = MD5.getInstance().getMD5String(content);
			cacheData.setUseLocalConfigInfo(true);
			cacheData.setLocalConfigInfoVersion(path.lastModified());
			cacheData.setContent(content);

			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(env, dataId, group, tenant);
			cacheData.setEncryptedDataKey(encryptedDataKey);

			log.warn(env.getName(),
					"[failover-change] failover file created. dataId={}, group={}, tenant={}, md5={}, content={}",
					dataId, group, tenant, md5, ContentUtils.truncateContent(content));
			return;
		}

		// �� -> û�С���֪ͨҵ�����������server�õ����ú�֪ͨ��
		if (cacheData.isUseLocalConfigInfo() && !path.exists()) {
			cacheData.setUseLocalConfigInfo(false);
			log.warn(env.getName(), "[failover-change] failover file deleted. dataId={}, group={}, tenant={}", dataId,
					group, tenant);
			return;
		}

		// �б��
		if (cacheData.isUseLocalConfigInfo() && path.exists()
				&& cacheData.getLocalConfigInfoVersion() != path.lastModified()) {
			String content = LocalConfigInfoProcessor.getFailover(env, dataId, group, tenant);
			String md5 = MD5.getInstance().getMD5String(content);
			cacheData.setUseLocalConfigInfo(true);
			cacheData.setLocalConfigInfoVersion(path.lastModified());
			cacheData.setContent(content);

			String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(env, dataId, group, tenant);
			cacheData.setEncryptedDataKey(encryptedDataKey);

			log.warn(env.getName(),
					"[failover-change] failover file changed. dataId={}, group={}, tenant={}, md5={}, content={}",
					dataId, group, tenant, md5, ContentUtils.truncateContent(content));
			return;
		}
	}

	public void checkConfigInfo() {
		checkConfigInfo(env);
	}

	public void checkConfigInfo(DiamondEnv env) {
		
		// ������
		int listenerSize = env.getAllCacheDataSize();
		// ����ȡ��Ϊ����
		int longingTaskCount = (int) Math.ceil(listenerSize / env.getPER_TASK_CONFIG_SIZE());
		if (longingTaskCount > currentLongingTaskCount) {
			for (int i = (int) currentLongingTaskCount; i < longingTaskCount; i++) {
				// Ҫ�ж������Ƿ���ִ�� �����Ҫ�ú����롣 �����б�����������ġ��仯��̿���������
				executorService.execute(new LongPullingRunnable(i));
			}
			currentLongingTaskCount = longingTaskCount;
		}
		
	}

	/**
	 * ��DiamondServer��ȡֵ�仯�˵�DataID�б?���صĶ�����ֻ��dataId��group����Ч�ġ� ��֤������NULL��
	 */
	List<String> checkUpdateDataIds(List<CacheData> cacheDatas, List<String> inInitializingCacheList) {
		if (MockServer.isTestMode()) {
			// ���� test mode cpu% ���
			try {
				Thread.sleep(3000l);
			} catch (InterruptedException e) {
			}
			List<String> updateList = new ArrayList<String>();
			for (CacheData cacheData : cacheDatas) {
				if (cacheData.isInitializing()) {
					// cacheData �״γ�����cacheMap��&�״�check����
					inInitializingCacheList
							.add(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant));
				}
				if (!CacheData
						.getMd5String(
								MockServer.getConfigInfo(cacheData.dataId, cacheData.group, cacheData.tenant, env))
						.equals(cacheData.getMd5())) {
					updateList.add(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant));
				}
			}
			return updateList;
		} else {
			StringBuilder sb = new StringBuilder();
			for (CacheData cacheData : cacheDatas) {
				if (!cacheData.isUseLocalConfigInfo()) {
					sb.append(cacheData.dataId).append(WORD_SEPARATOR);
					sb.append(cacheData.group).append(WORD_SEPARATOR);
					if (StringUtils.isBlank(cacheData.tenant)) {
						sb.append(cacheData.getMd5()).append(LINE_SEPARATOR);
					} else {
						sb.append(cacheData.getMd5()).append(WORD_SEPARATOR);
						sb.append(cacheData.getTenant()).append(LINE_SEPARATOR);
					}
					if (cacheData.isInitializing()) {
						// cacheData �״γ�����cacheMap��&�״�check����
						inInitializingCacheList
								.add(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant));
					}
				}
			}
			boolean isInitializingCacheList = !inInitializingCacheList.isEmpty();
			return checkUpdateConfigStr(sb.toString(), isInitializingCacheList);
		}
	}
	
	/**
	 * ��DiamondServer��ȡֵ�仯�˵�DataID�б?���صĶ�����ֻ��dataId��group����Ч�ġ� ��֤������NULL��
	 */
	List<String> checkUpdateConfigStr(String probeUpdateString, boolean isInitializingCacheList) {

		List<String> params = Arrays.asList(Constants.PROBE_MODIFY_REQUEST, probeUpdateString);
		long timeout = TimeUnit.SECONDS.toMillis(30L);

		List<String> headers = new ArrayList<String>(2);
		headers.add("longPullingTimeout");
		headers.add("" + timeout);
		
		// told server do not hang me up if new initializing cacheData added in 
		if (isInitializingCacheList) { 
			headers.add("longPullingNoHangUp");
			headers.add("true");
		}

		if (StringUtils.isBlank(probeUpdateString)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return Collections.emptyList();
		}

		try {
			HttpResult result = env.agent.httpPost("/config.co", headers, params, Constants.ENCODE, timeout);

			if (HttpURLConnection.HTTP_OK == result.code) {
				setHealthServer(true);
				return parseUpdateDataIdResponse(env, result.content);
			} else {
				setHealthServer(false);
				if (result.code == 500) {
					log.error("Diamond-0007", LoggerHelper.getErrorCodeStr("Diamond", "Diamond-0007", "��������",
							"[check-update] get changed dataId error"));
				}
				log.error(env.getName(), "DIAMOND-XXXX", "[check-update] get changed dataId error, code={}",
						result.code);
			}
		} catch (IOException e) {
			setHealthServer(false);
			log.error(env.getName(), "DIAMOND-XXXX", "[check-update] get changed dataId exception, msg={}",
					e.toString());
		}
		return Collections.emptyList();
	}


	/**
	 * ��HTTP��Ӧ�õ��仯��groupKey����֤������NULL��
	 */
	private List<String> parseUpdateDataIdResponse(DiamondEnv env, String response) {
		if (StringUtils.isBlank(response)) {
			return Collections.emptyList();
		}

		try {
			response = URLDecoder.decode(response, "UTF-8");
		} catch (Exception e) {
			log.error(env.getName(), "DIAMOND-XXXX", "[polling-resp] decode modifiedDataIdsString error", e);
		}

		List<String> updateList = new LinkedList<String>();

		for (String dataIdAndGroup : response.split(LINE_SEPARATOR)) {
			if (!StringUtils.isBlank(dataIdAndGroup)) {
				String [] keyArr = dataIdAndGroup.split(WORD_SEPARATOR);
				String dataId = keyArr[0];
				String group = keyArr[1];
				if (keyArr.length == 2) {
					updateList.add(GroupKey.getKey(dataId, group));
					log.info(env.getName(), "[polling-resp] config changed. dataId={}, group={}", dataId, group);
				} else if (keyArr.length == 3) {
					String tenant = keyArr[2];
					updateList.add(GroupKey.getKeyTenant(dataId, group, tenant));
					log.info(env.getName(), "[polling-resp] config changed. dataId={}, group={}, tenant={}", dataId,
							group, tenant);
				} else
				{
					log.error(env.getName(), "DIAMOND-XXXX", "[polling-resp] invalid dataIdAndGroup error", dataIdAndGroup);
				}
			}
		}
		return updateList;
	}


	ClientWorker(final DiamondEnv env) {
		this.env = env;
		executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("com.taobao.diamond.client.Worker." + env.serverMgr.name);
				t.setDaemon(true);
				return t;
			}
		});
		
		executorService = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("com.taobao.diamond.client.Worker.longPulling" + env.serverMgr.name);
				t.setDaemon(true);
				return t;
			}
		});
		
		executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				try {
					checkConfigInfo();
				} catch (Throwable e) {
					log.error(env.getName(), "DIAMOND-XXXX", "[sub-check] rotate check error", e);
				}
			}
		}, 1L, 10L, TimeUnit.MILLISECONDS);
	}
	
	class LongPullingRunnable implements Runnable {
		private int taskId;

		public LongPullingRunnable(int taskId) {
			this.taskId = taskId;
		}

		public void run() {
			try {
				List<CacheData> cacheDatas = new ArrayList<CacheData>();
				// check failover config
				for (CacheData cacheData : env.getAllCacheDataSnapshot()) {
					if (cacheData.getTaskId() == taskId) {
						cacheDatas.add(cacheData);
						try {
							checkLocalConfig(env, cacheData);
							if (cacheData.isUseLocalConfigInfo()) {
								cacheData.checkListenerMd5();
							}
						} catch (Exception e) {
							log.error("DIAMOND-CLIENT", "get local config info error", e);
						}
					}
				}

				List<String> inInitializingCacheList = new ArrayList<String>();
				// check server config
				List<String> changedGroupKeys = checkUpdateDataIds(cacheDatas, inInitializingCacheList);
				for (String groupKey : changedGroupKeys) {
					String key[] = GroupKey.parseKey(groupKey);
					String dataId = key[0];
					String group = key[1];
					String tenant = null;
					if (key.length == 3) {
						tenant = key[2];
					}
					try {
						CacheData cache = env.getCache(dataId, group, tenant);

						ConfigInfo configInfo;
						//��һ�μ������������ʱSLI
						if (cache.isInitializing()) {
							configInfo = getServerConfig(env, dataId, group, tenant, false, 3000L);
						} else {
							configInfo = getServerConfig(env, dataId, group, tenant, true, 3000L);
						}

						String content = null;
						String encryptedDataKey = null;
						if (configInfo != null) {
							content = configInfo.getContent();
							encryptedDataKey = configInfo.getEncryptedDataKey();
						}

						cache.setContent(content);
						cache.setEncryptedDataKey(encryptedDataKey);

						log.info(env.getName(), "[data-received] dataId={}, group={}, tenant={}, md5={}, content={}", dataId,
								group, tenant, cache.getMd5(), ContentUtils.truncateContent(content));
					} catch (DiamondException ioe) {
						log.error(env.getName(), "DIAMOND-XXXX",
								"[get-update] get changed config exception. dataId={}, group={}, tenant={}, msg={}",
								dataId, group, tenant, ioe.toString());
					}
				}
				for (CacheData cacheData : cacheDatas) {
					// cacheData.checkListenerMd5();
					if (!cacheData.isInitializing() || inInitializingCacheList
							.contains(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant))) {
						cacheData.checkListenerMd5();
						cacheData.setInitializing(false);
					}
				}
				inInitializingCacheList.clear();
			} catch (Throwable e) {
				log.error("500", "longPulling error", e);
			} finally {
				executorService.execute(this);
			}
		}

	}

	// =================

	public boolean isHealthServer() {
		return isHealthServer;
	}

	private void setHealthServer(boolean isHealthServer) {
		this.isHealthServer = isHealthServer;
	}

	final ScheduledExecutorService executor;
	final ExecutorService executorService;
	final DiamondEnv env;
	private boolean isHealthServer = true;
	private double currentLongingTaskCount = 0;
}
