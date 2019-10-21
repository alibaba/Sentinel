package com.taobao.diamond.domain;

public class ConfigInfoBatch extends ConfigInfo {

	/**
	 *
	 */
	private static final long serialVersionUID = 296578467953931353L;

	private int delimiter;


	public ConfigInfoBatch() {
	}

	public ConfigInfoBatch(String dataId, String group, int delimiter, String appName, String content) {
		super(dataId, group, appName, content);
		this.delimiter = delimiter;
	}

	public int getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(int delimiter) {
		this.delimiter = delimiter;
	}
	
	
}
