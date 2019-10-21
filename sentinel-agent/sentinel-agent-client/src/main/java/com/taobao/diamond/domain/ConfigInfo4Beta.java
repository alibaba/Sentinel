package com.taobao.diamond.domain;

public class ConfigInfo4Beta extends ConfigInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 296578467953931353L;

	private String betaIps;
	
	
	public ConfigInfo4Beta() {
	}

	public ConfigInfo4Beta(String dataId, String group, String appName, String content, String betaIps) {
		super(dataId, group, appName, content);
		this.betaIps = betaIps;
	}

	public String getBetaIps() {
		return betaIps;
	}

	public void setBetaIps(String betaIps) {
		this.betaIps = betaIps;
	}
	
	
}
