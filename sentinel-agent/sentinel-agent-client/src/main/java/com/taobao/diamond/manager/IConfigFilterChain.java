package com.taobao.diamond.manager;

import com.taobao.diamond.exception.DiamondException;

public interface IConfigFilterChain {

	public void doFilter(IConfigRequest request, IConfigResponse response) throws DiamondException;
	
}
