package com.alibaba.edas.acm.filter;

import java.util.HashMap;
import java.util.Map;
import com.taobao.diamond.manager.IFilterConfig;

public class KMSFilterConfig implements IFilterConfig {

	private Map<String, Object> initParameter = new HashMap<String, Object>();
	private String filterName;

	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public Object getInitParameter(String name) {
		return initParameter.get(name);
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public void addInitParamter(String key, Object value) {
		initParameter.put(key, value);
	}

}
