package com.taobao.diamond.domain;

/**
 * 配置信息类
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class ConfigInfo extends ConfigInfoBase {
	static final long serialVersionUID = -1L;

	private String tenant;

	private String appName;

	private String encryptedDataKey;

	public ConfigInfo() {

	}

	public ConfigInfo(String dataId, String group, String content) {
		super(dataId, group, content);
	}

	public ConfigInfo(String dataId, String group, String appName, String content) {
		super(dataId, group, content);
		this.appName = appName;
	}
	
	public ConfigInfo(String dataId, String group, String tenant, String appName, String content) {
		super(dataId, group, content);
		this.tenant = tenant;
		this.appName = appName;
	}

	public ConfigInfo(String dataId, String group, String tenant, String appName, String content,
	                  String encryptedDataKey) {
		this(dataId, group, tenant, appName, content);
		this.encryptedDataKey = encryptedDataKey;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getEncryptedDataKey() {
		return encryptedDataKey;
	}

	public void setEncryptedDataKey(String encryptedDataKey) {
		this.encryptedDataKey = encryptedDataKey;
	}

	@Override
	public String toString() {
		return "ConfigInfo{" + "id=" + getId() + ", dataId='" + getDataId() + '\'' + ", group='" + getGroup() + '\''
				+ ", tenant='" + tenant + '\'' + ", appName='" + appName + '\'' + ", content='" + getContent() + '\''
				+ ", md5='" + getMd5() + '\'' + '}';
	}

}
