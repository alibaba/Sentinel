package com.alibaba.jvm.sandbox.api.util;

/**
 * 数组操作工具类
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class GaArrayUtils {

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @param <T>   数组类型
     * @return TRUE:数组为空(null或length==0);FALSE:数组不为空
     */
    public static <T> boolean isEmpty(T[] array) {
        return null == array
                || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @param <T>   数组类型
     * @return TRUE:数组不为空;FALSE:数组为空(null或length==0)
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    /**
     * 获取数组长度
     *
     * @param array 数组
     * @param <T>   数组类型
     * @return 数组长度(null为0)
     */
    public static <T> int getLength(T[] array) {
        return isNotEmpty(array)
                ? array.length
                : 0;
    }

}
