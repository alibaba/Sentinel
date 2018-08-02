package com.alibaba.csp.sentinel.transport.dubbo.server;

import com.alibaba.csp.sentinel.annotation.annotation.Sentinel;

public class UserServiceImpl implements UserService{

	
	@Sentinel(name="laohu")
	public String getUserName() {
		String str = "Hello sentiniel";
		System.out.println( str );
		return str;
	}

}
