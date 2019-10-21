package com.alibaba.jvm.sandbox.api.util;

/**
 * 字符串操作工具类
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class GaStringUtils {

    /**
     * 提取Class的类名
     * <p>
     * 默认情况下提取{@link Class#getCanonicalName()}，但在遇到不可见类时提取{@link Class#getName()}
     * </p>
     *
     * @param clazz Class类
     * @return Class类名
     */
    public static String getJavaClassName(final Class<?> clazz) {
        return clazz.isArray()
                ? clazz.getCanonicalName()
                : clazz.getName();
    }

    /**
     * 提取Class数组的类名数组
     *
     * @param classArray Class数组
     * @return 类名数组
     */
    public static String[] getJavaClassNameArray(final Class<?>[] classArray) {
        if (GaArrayUtils.isEmpty(classArray)) {
            return null;
        }
        final String[] javaClassNameArray = new String[classArray.length];
        for (int index = 0; index < classArray.length; index++) {
            javaClassNameArray[index] = getJavaClassName(classArray[index]);
        }
        return javaClassNameArray;
    }

    /**
     * 字符串是否为空
     * <p>
     * null和""均为空
     * </p>
     *
     * @param string 目标字符串
     * @return TRUE:空字符串;FALSE:非空字符串
     */
    public static boolean isEmpty(String string) {
        return null == string
                || string.isEmpty();
    }

    /**
     * 通配符表达式匹配
     * <p>
     * 通配符是一种特殊语法，主要有星号(*)和问号(?)组成，在Sandbox中主要用来模糊匹配类名和方法名。
     * 比如：java.lang.String，可以被"*String"所匹配
     * </p>
     * <ul>
     * <li>(null) matching (null) == false</li>
     * <li>    ANY matching ("*") == true</li>
     * </ul>
     *
     * @param string   目标字符串
     * @param wildcard 通配符匹配模版
     * @return true:目标字符串符合匹配模版;false:目标字符串不符合匹配模版
     */
    public static boolean matching(final String string, final String wildcard) {
        return null != wildcard
                && null != string
                && matching(string, wildcard, 0, 0);
    }

    /**
     * Internal matching recursive function.
     */
    private static boolean matching(String string, String wildcard, int stringStartNdx, int patternStartNdx) {
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = wildcard.length();
        if (pLen == 1) {
            if (wildcard.charAt(0) == '*') {     // speed-up
                return true;
            }
        }
        int sLen = string.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // check if end of string and/or pattern occurred
            if ((sNdx >= sLen)) {   // end of string still may have pending '*' callback pattern
                while ((pNdx < pLen) && (wildcard.charAt(pNdx) == '*')) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            if (pNdx >= pLen) {         // end of pattern, but not end of the string
                return false;
            }
            char p = wildcard.charAt(pNdx);    // pattern char

            // perform logic
            if (!nextIsNotWildcard) {

                if (p == '\\') {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == '?') {
                    sNdx++;
                    pNdx++;
                    continue;
                }
                if (p == '*') {
                    char pnext = 0;           // next pattern char
                    if (pNdx + 1 < pLen) {
                        pnext = wildcard.charAt(pNdx + 1);
                    }
                    if (pnext == '*') {         // double '*' have the same effect as one '*'
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = string.length(); i >= sNdx; i--) {
                        if (matching(string, wildcard, i, pNdx)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            // check if pattern char and string char are equals
            if (p != string.charAt(sNdx)) {
                return false;
            }

            // everything matches for now, continue
            sNdx++;
            pNdx++;
        }
    }

}
