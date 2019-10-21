package com.taobao.diamond.client.impl;

import com.taobao.diamond.utils.StringUtils;

public class TenantUtil {

	static String userTenant = "";
	
	static {
		userTenant = System.getProperty("tenant.id", "");
		if (StringUtils.isBlank(userTenant)) {
			userTenant = System.getProperty("acm.namespace", "");
		}
	}

	public static String getUserTenant() {
		return userTenant;
	}

	public static void setUserTenant(String userTenant) {
		TenantUtil.userTenant = userTenant;
	}
}
