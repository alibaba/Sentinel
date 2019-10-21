package com.taobao.diamond.client;

import com.taobao.diamond.client.impl.LocalConfigInfoProcessor;

public class SnapShotSwitch {

	// �Ƿ񱾵ر������ÿ��أ�Ĭ�ϱ��汾������
	private static Boolean isSnapShot = true;

	public static Boolean getIsSnapShot() {
		return isSnapShot;
	}

	public static void setIsSnapShot(Boolean isSnapShot) {
		SnapShotSwitch.isSnapShot = isSnapShot;
		LocalConfigInfoProcessor.cleanAllSnapshot();
	}
	
}
