package com.taobao.diamond.manager;

public interface IConfigResponse {
	
	public Object getParameter(String key);

	public IConfigContext getConfigContext();

}
