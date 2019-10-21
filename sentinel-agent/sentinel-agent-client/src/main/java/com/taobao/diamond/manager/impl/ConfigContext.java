package com.taobao.diamond.manager.impl;

import java.util.HashMap;
import java.util.Map;

import com.taobao.diamond.manager.IConfigContext;

public class ConfigContext implements IConfigContext {

	private Map<String, Object> param = new HashMap<String, Object>();

	@Override
	public Object getParameter(String key) {
		return param.get(key);
	}

	@Override
	public void setParameter(String key, Object value) {
		param.put(key, value);
	}

}
