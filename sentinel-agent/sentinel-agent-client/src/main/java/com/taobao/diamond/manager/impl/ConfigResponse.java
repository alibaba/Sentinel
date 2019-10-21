package com.taobao.diamond.manager.impl;

import java.util.HashMap;
import java.util.Map;

import com.taobao.diamond.manager.IConfigContext;
import com.taobao.diamond.manager.IConfigResponse;

public class ConfigResponse implements IConfigResponse {

	private Map<String, Object> param = new HashMap<String, Object>();

	private IConfigContext configContext = new ConfigContext();

	public String getTenant() {
		return (String) param.get("tenant");
	}

	public void setTenant(String tenant) {
		param.put("tenant", tenant);
	}

	public String getDataId() {
		return (String) param.get("dataId");
	}

	public void setDataId(String dataId) {
		param.put("dataId", dataId);
	}

	public String getGroup() {
		return (String) param.get("group");
	}

	public void setGroup(String group) {
		param.put("group", group);
	}

	public String getContent() {
		return (String) param.get("content");
	}

	public void setContent(String content) {
		param.put("content", content);
	}

	public String getEncryptedDataKey() {
		return (String) param.get("encryptedDataKey");
	}

	public void setEncryptedDataKey(String encryptedDataKey) {
		param.put("encryptedDataKey", encryptedDataKey);
	}

	@Override
	public Object getParameter(String key) {
		return param.get(key);
	}

	@Override
	public IConfigContext getConfigContext() {
		return configContext;
	}

}
