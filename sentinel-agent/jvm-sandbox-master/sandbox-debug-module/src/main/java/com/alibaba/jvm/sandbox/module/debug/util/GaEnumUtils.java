package com.alibaba.jvm.sandbox.module.debug.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 枚举工具类
 *
 * @author oldmanpushcart@gmail.com
 */
public class GaEnumUtils {

    public static <T extends Enum<T>> Set<T> valuesOf(Class<T> enumClass, String[] enumNameArray, T[] defaultEnumArray) {
        final Set<T> enumSet = new LinkedHashSet<T>();
        if (ArrayUtils.isNotEmpty(enumNameArray)) {
            for (final String enumName : enumNameArray) {
                final T enumValue = EnumUtils.getEnum(enumClass, enumName);
                if (null != enumValue) {
                    enumSet.add(enumValue);
                }
            }
        }
        if (CollectionUtils.isEmpty(enumSet)
                && ArrayUtils.isNotEmpty(defaultEnumArray)) {
            Collections.addAll(enumSet, defaultEnumArray);
        }
        return enumSet;
    }

}
