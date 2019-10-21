package com.taobao.diamond.manager;

public interface IConfigRequest {

	public Object getParameter(String key);

	public IConfigContext getConfigContext();

}
