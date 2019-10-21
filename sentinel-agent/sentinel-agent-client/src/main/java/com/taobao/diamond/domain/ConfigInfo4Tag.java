package com.taobao.diamond.domain;

public class ConfigInfo4Tag extends ConfigInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 296578467953931353L;

	private String tag;
	
	
	public ConfigInfo4Tag() {
	}

	public ConfigInfo4Tag(String dataId, String group, String tag, String appName, String content) {
		super(dataId, group, appName, content);
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	
}
