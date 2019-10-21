package com.taobao.diamond.utils;

import java.util.List;

import com.taobao.diamond.exception.DiamondException;

/**
 * 参数合法性检查工具类
 * 
 * @author zh
 * 
 */
public class ParamUtils {

    private static char[] validChars = new char[] { '_', '-', '.', ':' };


    /**
     * 白名单的方式检查, 合法的参数只能包含字母、数字、以及validChars中的字符, 并且不能为空
     * 
     * @param param
     * @return
     */
    public static boolean isValid(String param) {
        if (param == null) {
            return false;
        }
        int length = param.length();
        for (int i = 0; i < length; i++) {
            char ch = param.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                continue;
            }
            else if (isValidChar(ch)) {
                continue;
            }
            else {
                return false;
            }
        }
        return true;
    }


    private static boolean isValidChar(char ch) {
        for (char c : validChars) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }
    
	public static void checkKeyParam(String dataId, String group) throws DiamondException {
		if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "group invalid");
		}
	}
	
	public static void checkTDG(String tenant, String dataId, String group) throws DiamondException {
		checkTenant(tenant);
		if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "group invalid");
		}
	}
	
	public static void checkKeyParam(String dataId, String group, String datumId)
			throws DiamondException {
		if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "group invalid");
		}
		if (StringUtils.isBlank(datumId) || !ParamUtils.isValid(datumId)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "datumId invalid");
		}
	}
    
	public static void checkKeyParam(List<String> dataIds, String group) throws DiamondException {
		if (dataIds == null || dataIds.size() == 0) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "dataIds invalid");
		}
		for (String dataId : dataIds) {
			if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
				throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "dataId invalid");
			}
		}
		if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "group invalid");
		}
	}

	public static void checkParam(String dataId, String group, String content) throws DiamondException {
		checkKeyParam(dataId, group);
		if (StringUtils.isBlank(content)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}
    
	public static void checkParam(String dataId, String group, String datumId, String content) throws DiamondException {
		checkKeyParam(dataId, group, datumId);
		if (StringUtils.isBlank(content)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}

	public static void checkTenant(String tenant) throws DiamondException {
		if (StringUtils.isBlank(tenant) || !ParamUtils.isValid(tenant)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "tenant invalid");
		}
	}
	
	public static void checkBetaIps(String betaIps) throws DiamondException {
		if (StringUtils.isBlank(betaIps)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "betaIps invalid");
		}
		String ipsArr[] = betaIps.split(",");
		for (String ip : ipsArr) {
			if (!IPUtil.isIPV4(ip)) {
				throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "betaIps invalid");
			}
		}
	}

	public static void checkContent(String content) throws DiamondException {
		if (StringUtils.isBlank(content)) {
			throw new DiamondException(DiamondException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}
}
