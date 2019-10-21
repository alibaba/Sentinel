package com.taobao.diamond.manager;

public interface IConfigContext {
	
	public Object getParameter(String key);

	public void setParameter(String key, Object value);
}
