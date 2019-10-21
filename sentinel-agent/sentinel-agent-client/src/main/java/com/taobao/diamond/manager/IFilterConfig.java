package com.taobao.diamond.manager;

public interface IFilterConfig {

	/**
	 * get filter name
	 * 
	 * @return
	 */
	public String getFilterName();

	/**
	 * get param
	 * 
	 * @param name
	 * @return param
	 */
	public Object getInitParameter(String name);

}
