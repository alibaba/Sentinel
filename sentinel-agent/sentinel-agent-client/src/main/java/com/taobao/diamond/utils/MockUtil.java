package com.taobao.diamond.utils;

import com.taobao.diamond.mockserver.MockServer;

public class MockUtil {
	
	public static boolean isMock() {
		return isMock;
	}

	public static void setMock(boolean isDebug) {
		MockUtil.isMock = isDebug;
		if (isDebug) {
			MockServer.setUpMockServer();
		} else {
			MockServer.tearDownMockServer();
		}
	}

	static boolean isMock = true;
	
	static {
		String isDebugStr = System.getProperty("diamond.mock", "fasle");
		isMock = Boolean.valueOf(isDebugStr);
		if (isMock) {
			MockServer.setUpMockServer();
		} else {
			MockServer.tearDownMockServer();
		}
	}
	
}
