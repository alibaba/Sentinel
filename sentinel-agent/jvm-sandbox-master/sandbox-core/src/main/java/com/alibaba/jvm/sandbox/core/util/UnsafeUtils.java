package com.alibaba.jvm.sandbox.core.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Sum Jvm Unsafe
 * Created by luanjia on 16/10/15.
 */
public class UnsafeUtils {

    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

}
