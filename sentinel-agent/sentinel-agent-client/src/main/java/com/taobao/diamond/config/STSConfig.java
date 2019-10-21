package com.taobao.diamond.config;

import com.taobao.diamond.utils.StringUtils;

/**
 * @author hexu.hxy
 * @date 2018/5/2
 */
public class STSConfig {
	private static final String RAM_SECURITY_CREDENTIALS_URL
		= "http://100.100.100.200/latest/meta-data/ram/security-credentials/";
	private String ramRoleName;
	/**
	 * STS ��ʱƾ֤��Ч��ʣ�����ʱ��ʼˢ�£����?��ʱ��� STS ����ʱ��������ã�
	 */
	private int timeToRefreshInMillisecond = 3 * 60 * 1000;
	/**
	 * ��ȡ STS ��ʱƾ֤��Ԫ��ݽӿڣ����ɫ��ƣ�
	 */
	private String securityCredentialsUrl;
	/**
	 * �趨 STS ��ʱƾ֤������ͨ��Ԫ��ݽӿڻ�ȡ
	 */
	private String securityCredentials;
	/**
	 * �Ƿ񻺴�
	 */
	private boolean cacheSecurityCredentials = true;

	private static class Singleton {
		private static final STSConfig INSTANCE = new STSConfig();
	}

	private STSConfig() {
		String ramRoleName = System.getProperty("ram.role.name");
		if (!StringUtils.isBlank(ramRoleName)) {
			setRamRoleName(ramRoleName);
		}

		String timeToRefreshInMillisecond = System.getProperty("time.to.refresh.in.millisecond");
		if (!StringUtils.isBlank(timeToRefreshInMillisecond)) {
			setTimeToRefreshInMillisecond(Integer.parseInt(timeToRefreshInMillisecond));
		}

		String securityCredentials = System.getProperty("security.credentials");
		if (!StringUtils.isBlank(securityCredentials)) {
			setSecurityCredentials(securityCredentials);
		}

		String securityCredentialsUrl = System.getProperty("security.credentials.url");
		if (!StringUtils.isBlank(securityCredentialsUrl)) {
			setSecurityCredentialsUrl(securityCredentialsUrl);
		}

		String cacheSecurityCredentials = System.getProperty("cache.security.credentials");
		if (!StringUtils.isBlank(cacheSecurityCredentials)) {
			setCacheSecurityCredentials(Boolean.valueOf(cacheSecurityCredentials));
		}
	}

	public static STSConfig getInstance() {
		return Singleton.INSTANCE;
	}

	public String getRamRoleName() {
		return ramRoleName;
	}

	public void setRamRoleName(String ramRoleName) {
		this.ramRoleName = ramRoleName;
	}

	public int getTimeToRefreshInMillisecond() {
		return timeToRefreshInMillisecond;
	}

	public void setTimeToRefreshInMillisecond(int timeToRefreshInMillisecond) {
		this.timeToRefreshInMillisecond = timeToRefreshInMillisecond;
	}

	public String getSecurityCredentialsUrl() {
		if (securityCredentialsUrl == null && ramRoleName != null) {
			return RAM_SECURITY_CREDENTIALS_URL + ramRoleName;
		}
		return securityCredentialsUrl;
	}

	public void setSecurityCredentialsUrl(String securityCredentialsUrl) {
		this.securityCredentialsUrl = securityCredentialsUrl;
	}

	public String getSecurityCredentials() {
		return securityCredentials;
	}

	public void setSecurityCredentials(String securityCredentials) {
		this.securityCredentials = securityCredentials;
	}

	public boolean isSTSOn() {
		return StringUtils.isNotEmpty(getSecurityCredentials()) || StringUtils.isNotEmpty(getSecurityCredentialsUrl());
	}

	public boolean isCacheSecurityCredentials() {
		return cacheSecurityCredentials;
	}

	public void setCacheSecurityCredentials(boolean cacheSecurityCredentials) {
		this.cacheSecurityCredentials = cacheSecurityCredentials;
	}
}
