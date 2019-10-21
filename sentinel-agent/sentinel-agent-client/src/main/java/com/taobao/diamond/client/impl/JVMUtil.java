package com.taobao.diamond.client.impl;

import com.taobao.middleware.logger.Logger;

/**
 * get jvm config
 * 
 * @author water.lyl
 *
 */
public class JVMUtil {

	/**
	 * whether is multi instance
	 * 
	 * @return whether multi
	 */
	public static Boolean isMultiInstance() {
		return isMultiInstance;
	}

	private static Boolean isMultiInstance = false;
	private static String TRUE = "true";
	static final public Logger log = LogUtils.logger(JVMUtil.class);

	static {
		String multiDeploy = System.getProperty("isMultiInstance", "false");
		if (TRUE.equals(multiDeploy)) {
			isMultiInstance = true;
		}
		log.info("isMultiInstance:{}", isMultiInstance);
	}
}
