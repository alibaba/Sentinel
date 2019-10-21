package com.taobao.diamond.utils;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.util.List;
import java.util.Map;

/**
 * env util.
 * 
 * @author Diamond
 *
 */
public class EnvUtil {

	public static void setSelfEnv(Map<String, List<String>> headers) {
		if (headers != null) {
			List<String> amorayTagTmp = headers.get(AMORY_TAG);
			if (amorayTagTmp == null) {
				if (selfAmorayTag != null) {
					selfAmorayTag = null;
					log.warn("selfAmoryTag:null");
				}
			} else {
				String amorayTagTmpStr = listToString(amorayTagTmp);
				if (!amorayTagTmpStr.equals(selfAmorayTag)) {
					selfAmorayTag = amorayTagTmpStr;
					log.warn("selfAmoryTag:{}", selfAmorayTag);
				}
			}

			List<String> vipserverTagTmp = headers.get(VIPSERVER_TAG);
			if (vipserverTagTmp == null) {
				if (selfVipserverTag != null) {
					selfVipserverTag = null;
					log.warn("selfVipserverTag:null");
				}
			} else {
				String vipserverTagTmpStr = listToString(vipserverTagTmp);
				if (!vipserverTagTmpStr.equals(selfVipserverTag)) {
					selfVipserverTag = vipserverTagTmpStr;
					log.warn("selfVipserverTag:{}", selfVipserverTag);
				}
			}
			List<String> locationTagTmp = headers.get(LOCATION_TAG);
			if (locationTagTmp == null) {
				if (selfLocationTag != null) {
					selfLocationTag = null;
					log.warn("selfLocationTag:null");
				}
			} else {
				String locationTagTmpStr = listToString(locationTagTmp);
				if (!locationTagTmpStr.equals(selfLocationTag)) {
					selfLocationTag = locationTagTmpStr;
					log.warn("selfLocationTag:{}", selfLocationTag);
				}
			}
		}
	}

	public static String getSelfAmorayTag() {
		return selfAmorayTag;
	}

	public static String getSelfVipserverTag() {
		return selfVipserverTag;
	}

	public static String getSelfLocationTag() {
		return selfLocationTag;
	}
	
	public static String listToString(List<String> list) {
		if (list == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		boolean first = true;
		// ��һ��ǰ�治ƴ��","
		for (String string : list) {
			if (first) {
				first = false;
			} else {
				result.append(",");
			}
			result.append(string);
		}
		return result.toString();
	}

	private static String selfAmorayTag;
	private static String selfVipserverTag;
	private static String selfLocationTag;
	public final static String AMORY_TAG = "Amory-Tag";
	public final static String VIPSERVER_TAG = "Vipserver-Tag";
	public final static String LOCATION_TAG = "Location-Tag";
}
