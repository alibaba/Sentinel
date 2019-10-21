package com.taobao.diamond.domain;

import java.io.Serializable;

/**
 * config key
 * 
 * @author Diamond
 *
 */
public class ConfigKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1748953484511867580L;

	private String appName;
	private String dataId;
	private String group;

	public ConfigKey() {
	};

	public ConfigKey(String appName, String dataId, String group) {
		this.appName = appName;
		this.dataId = dataId;
		this.group = group;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
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

}
