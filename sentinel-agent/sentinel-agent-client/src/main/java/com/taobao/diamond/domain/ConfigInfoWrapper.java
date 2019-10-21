package com.taobao.diamond.domain;

public class ConfigInfoWrapper extends ConfigInfo {
	private static final long serialVersionUID = 4511997359365712505L;

	private long lastModified;

	public ConfigInfoWrapper() {
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
