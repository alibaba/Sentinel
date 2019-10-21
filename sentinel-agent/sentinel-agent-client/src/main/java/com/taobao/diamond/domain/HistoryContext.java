package com.taobao.diamond.domain;

public class HistoryContext {
	public String serverId;
    public String dataId;
    public String group;
    public String tenant;
	private String appName;
    public boolean success;
    public int statusCode;
    public String statusMsg;
    public Page<ConfigHistoryInfo> configs;


	public HistoryContext(String serverId, String dataId, String group, int statusCode, String statusMsg,
			Page<ConfigHistoryInfo> configs) {
		this.serverId = serverId;
		this.dataId = dataId;
		this.group = group;
		this.statusCode = statusCode;
		this.statusMsg = statusMsg;
		this.configs = configs;
		this.success = 200 == statusCode;
	}
	
	public HistoryContext() {
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
    public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public Page<ConfigHistoryInfo> getConfigs() {
		return configs;
	}

	public void setConfigs(Page<ConfigHistoryInfo> configs) {
		this.configs = configs;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

}
