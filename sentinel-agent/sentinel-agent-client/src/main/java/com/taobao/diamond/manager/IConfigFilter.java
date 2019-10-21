package com.taobao.diamond.manager;

import com.taobao.diamond.exception.DiamondException;

public interface IConfigFilter {

	void init(IFilterConfig filterConfig);

	void doFilter(IConfigRequest request, IConfigResponse response, IConfigFilterChain filterChain)
			throws DiamondException;

	void deploy();
	
	int getOrder();
	
	String getFilterName();
	
}
