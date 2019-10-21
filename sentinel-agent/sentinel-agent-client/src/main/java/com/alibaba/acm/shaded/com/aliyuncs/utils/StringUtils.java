package com.alibaba.acm.shaded.com.aliyuncs.utils;

/**
 * @author VK.Gao
 * @date 2018/03/30
 */
public class StringUtils {

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

}
