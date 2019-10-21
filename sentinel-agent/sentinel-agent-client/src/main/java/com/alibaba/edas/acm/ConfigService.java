package com.alibaba.edas.acm;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.alibaba.edas.acm.domain.ConfigKey;
import com.alibaba.edas.acm.exception.ConfigException;
import com.alibaba.edas.acm.filter.IACMConfigFilter;
import com.alibaba.edas.acm.filter.KMSConfigFilter;
import com.alibaba.edas.acm.filter.KMSFilterConfig;
import com.alibaba.edas.acm.listener.ConfigChangeListenerAdapter;
import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.impl.DiamondEnvRepo;
import com.taobao.diamond.client.impl.ServerHttpAgent;
import com.taobao.diamond.client.impl.TenantUtil;
import com.taobao.diamond.config.STSConfig;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.identify.CredentialService;
import com.taobao.diamond.utils.JSONUtils;
import com.taobao.diamond.utils.StringUtils;

/**
 * 提供动态配置管理服务 Provide dynamic configuration management services
 * 
 * @author ConfigCenter
 *
 */
public class ConfigService {

	public static void init(String endpoint, String namespace, String accessKey, String secretKey) {
		Properties pp = new Properties();
		if (StringUtils.isNotEmpty(endpoint)) {
			pp.put("endpoint", endpoint);
		}
		if (StringUtils.isNotEmpty(namespace)) {
			pp.put("namespace", namespace);
		}
		if (StringUtils.isNotEmpty(accessKey)) {
			pp.put("accessKey", accessKey);
		}
		if (StringUtils.isNotEmpty(secretKey)) {
			pp.put("secretKey", secretKey);
		}
		init(pp);
	}

	public static void init() {
	}
	
	public static void init(Properties pp) {
		String endpoint = (String) pp.get("endpoint");
		String namespace = (String) pp.get("namespace");
		String accessKey = (String) pp.get("accessKey");
		String secretKey = (String) pp.get("secretKey");
		if (StringUtils.isNotEmpty(endpoint)) {
			String domainName = System.getenv("address_server_domain");
			if (StringUtils.isBlank(domainName)) {
				domainName = System.getProperty("address.server.domain");
				if (StringUtils.isBlank(domainName)) {
					ServerHttpAgent.domainName = endpoint;
				}
			}
		}
		if (StringUtils.isNotEmpty(namespace)) {
			String userTenant = System.getProperty("tenant.id", "");
			if (StringUtils.isBlank(userTenant)) {
				TenantUtil.setUserTenant(namespace);
			}
		}
		if (StringUtils.isNotEmpty(accessKey)) {
			if (StringUtils.isBlank(CredentialService.getInstance().getCredential().getAccessKey())) {
				CredentialService.getInstance().getCredential().setAccessKey(accessKey);
			}
		}
		if (StringUtils.isNotEmpty(secretKey)) {
			if (StringUtils.isBlank(CredentialService.getInstance().getCredential().getSecretKey())) {
				CredentialService.getInstance().getCredential().setSecretKey(secretKey);
			}
		}

		String ramRoleName = (String)pp.get("ramRoleName");
		if (!StringUtils.isBlank(ramRoleName)) {
			if (StringUtils.isBlank(System.getProperty("ram.role.name"))) {
				STSConfig.getInstance().setRamRoleName(ramRoleName);
			}
		}

		String securityCredentials = (String)pp.get("securityCredentials");
		if (!StringUtils.isBlank(securityCredentials)) {
			if (StringUtils.isBlank(System.getProperty("security.credentials"))) {
				STSConfig.getInstance().setSecurityCredentials(securityCredentials);
			}
		}

		boolean openKMSFilter = (pp.get("openKMSFilter") == null) ? false : (Boolean)pp.get("openKMSFilter");
		if (openKMSFilter) {
			KMSConfigFilter kmsConfigFilter = new KMSConfigFilter();
			KMSFilterConfig filterConfig = new KMSFilterConfig();
			filterConfig.addInitParamter("keyId", pp.get("keyId"));
			filterConfig.addInitParamter("regionId", pp.get("regionId"));
			filterConfig.addInitParamter("ramRoleName", STSConfig.getInstance().getRamRoleName());
			filterConfig.addInitParamter("accessKey", accessKey);
			filterConfig.addInitParamter("secretKey", secretKey);

			if (!StringUtils.isBlank(securityCredentials)) {
				filterConfig.addInitParamter("securityCredentials", securityCredentials);
			}

			kmsConfigFilter.init(filterConfig);
			ConfigService.addConfigFilter(kmsConfigFilter);
		}

		String timeToRefreshInMillisecond = (String)pp.get("timeToRefreshInMillisecond");
		if (!StringUtils.isBlank(timeToRefreshInMillisecond)) {
			if (StringUtils.isBlank(System.getProperty("time.to.refresh.in.millisecond"))) {
				STSConfig.getInstance().setTimeToRefreshInMillisecond(Integer.parseInt(timeToRefreshInMillisecond));
			}
		}

		String securityCredentialsUrl = (String)pp.get("securityCredentialsUrl");
		if (!StringUtils.isBlank(securityCredentialsUrl)) {
			if (StringUtils.isBlank(System.getProperty("security.credentials.url"))) {
				STSConfig.getInstance().setSecurityCredentialsUrl(securityCredentialsUrl);
			}
		}
		String cacheSecurityCredentials = (String)pp.get("cacheSecurityCredentials");
		if (!StringUtils.isBlank(cacheSecurityCredentials)) {
			if (StringUtils.isBlank(System.getProperty("cache.security.credentials"))) {
				STSConfig.getInstance().setCacheSecurityCredentials(Boolean.valueOf(cacheSecurityCredentials));
			}
		}
	}

	/**
	 * 添加对dataId的监听，在服务端修改该配置后，客户端会使用传入的listener回调应用。
	 * 推荐异步处理，应用可以实现ManagerListener中的getExecutor方法，提供执行的线程池。如果为提供，则使用主线程回调，
	 * 可能会阻塞其他配置或者被其他配置阻塞。
	 * 
	 * @param dataId
	 *            配置id
	 * @param group
	 *            配置分組
	 * @param listener
	 *            监听器
	 */
	public static void addListener(String dataId, String group, ConfigChangeListenerAdapter listener) {
		DiamondEnvRepo.getDefaultEnv().addTenantListeners(dataId, group, Arrays.asList(listener));
	}

	/**
	 * 对一个dataId同时添加多个Listener，发生配置变更后会依次回调Listener
	 * 
	 * @param dataId
	 *            配置id
	 * @param group
	 *            配置分組
	 * @param listener
	 *            监听器
	 */
	public static void addGlobalListener(String dataId, String group, ConfigChangeListenerAdapter listener) {
		DiamondEnvRepo.getDefaultEnv().addListeners(dataId, group, Arrays.asList(listener));
	}

	/**
	 * 按照本地容灾, server, 本地缓存的优先级获取配置。超时单位是毫秒。
	 * 
	 * @param dataId
	 *            配置id
	 * @param group
	 *            配置分組
	 * @param timeoutMs
	 *            获取超时时间
	 * @throws ConfigException
	 *             ConfigException
	 * @return 配置内容
	 */
	public static String getConfig(String dataId, String group, long timeoutMs) throws ConfigException {
		try {
			return DiamondEnvRepo.getDefaultEnv().getTenantConfig(dataId, group, timeoutMs);
		} catch (DiamondException e) {
			throw new ConfigException(e.getErrCode(), e.getErrMsg(), e);
		}
	}

	/**
	 * 按照本地容灾, server , 本地缓存的优先级获取配置。超时单位是毫秒。
	 * 
	 * @param dataId
	 *            配置id
	 * @param group
	 *            配置分組
	 * @param timeoutMs
	 *            获取超时时间
	 * @throws ConfigException
	 *             ConfigException
	 * @return 配置内容 配置内容自动从string转为properties文件
	 */
	public static Properties getConfig2Properties(String dataId, String group, long timeoutMs) throws ConfigException {
		Properties properties = new Properties();
		try {
			String content = DiamondEnvRepo.getDefaultEnv().getTenantConfig(dataId, group, timeoutMs);
			try {
				properties.load(new StringReader(content));
			} catch (IOException e) {
				throw new ConfigException(e.getMessage());
			}
		} catch (DiamondException diamondException) {
			throw new ConfigException(diamondException.getErrCode(), diamondException.getErrMsg(), diamondException);
		}
		return properties;
	}

	public static List<ConfigKey> getConfigs(long timeoutMs) throws ConfigException {
		try {
			List<com.taobao.diamond.domain.ConfigKey> configKeys = Diamond.getAllTenantConfig(timeoutMs);
			List<ConfigKey> configs = new ArrayList<ConfigKey>();

			for (com.taobao.diamond.domain.ConfigKey configKey : configKeys) {
				ConfigKey config = new ConfigKey(configKey.getDataId(), configKey.getGroup());
				configs.add(config);
			}

			return configs;
		} catch (DiamondException diamondException) {
			throw new ConfigException(diamondException.getErrCode(), diamondException.getErrMsg(), diamondException);
		}
	}

	/**
	 * 按照本地容灾 , server , 本地缓存的优先级获取配置。超时单位是毫秒。
	 * 
	 * @param dataId
	 *            配置id
	 * @param group
	 *            配置分組
	 * @param timeoutMs
	 *            获取超时时间
	 * @param clazz
	 *            Object.class
	 * @throws ConfigException
	 *             ConfigException
	 * @return 配置内容 可以转化为对应java类
	 */
	public static Object getConfig(String dataId, String group, long timeoutMs, Class<?> clazz) throws ConfigException {
		try {
			String jsonStr = DiamondEnvRepo.getDefaultEnv().getTenantConfig(dataId, group, timeoutMs);
			Object result = JSONUtils.deserializeObject(jsonStr, clazz);
			return result;
		} catch (IOException e) {
			throw new ConfigException(e.getMessage());
		} catch (DiamondException diamondException) {
			throw new ConfigException(diamondException.getErrCode(), diamondException.getErrMsg(), diamondException);
		}
	}
	
	/**
	 * 添加过滤器
	 * 
	 * @param configFilter
	 *            过滤器
	 */
	public static void addConfigFilter(IACMConfigFilter configFilter) {
		DiamondEnvRepo.getDefaultEnv().addConfigFilter(configFilter);
	}

	/**
	 * 创建或修改配置，无配置则创建，有配置则修改
	 * 
	 * @param dataId
	 *            配置ID
	 * @param group
	 *            配置分组
	 * @param content
	 *            内容
	 * @return 是否发布成功
	 * @throws ConfigException
	 *             写异常
	 */
	public static boolean publishConfig(String dataId, String group, String content) throws ConfigException {
		try {
			return DiamondEnvRepo.getDefaultEnv().publishTenantSingle(dataId, group, content);
		} catch (DiamondException e) {
			throw new ConfigException(e.getErrCode(), e.getErrMsg(), e);
		}
	}

	/**
	 * 删除配置
	 * 
	 * @param dataId
	 *            配置ID
	 * @param group
	 *            配置分组
	 * @return 是否删除成功
	 * @throws ConfigException
	 *             删异常
	 */
	public static boolean removeConfig(String dataId, String group) throws ConfigException {
		try {
			return DiamondEnvRepo.getDefaultEnv().removeTenantConfig(dataId, group);
		} catch (DiamondException e) {
			throw new ConfigException(e.getErrCode(), e.getErrMsg(), e);
		}
	}

}
