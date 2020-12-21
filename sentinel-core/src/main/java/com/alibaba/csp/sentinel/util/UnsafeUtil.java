/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.util;

import com.alibaba.csp.sentinel.slots.statistic.base.LongAdder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author machunxiao create at 2020-12-21
 */
public class UnsafeUtil {

  private static final Object UNSAFE;
  private static final Method COMPARE_AND_SET_LONG;
  private static final Method COMPARE_AND_SET_INT;
  private static final Method OBJECT_FIELD_OFFSET;

  static {
    Object unsafe;
    Method casLong;
    Method casInt;
    Method objectFieldOffset;
    try {
      Class<?> clazz = Class.forName("sun.misc.Unsafe");
      Field field = clazz.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      casLong = clazz.getMethod("compareAndSwapLong", Object.class, long.class, long.class, long.class);
      casInt = clazz.getMethod("compareAndSwapInt", Object.class, long.class, int.class, int.class);
      objectFieldOffset = clazz.getMethod("objectFieldOffset", Field.class);
      unsafe = field.get(null);
    } catch (Throwable e) {
      throw new Error(e);
    }
    UNSAFE = unsafe;
    COMPARE_AND_SET_LONG = casLong;
    COMPARE_AND_SET_INT = casInt;
    OBJECT_FIELD_OFFSET = objectFieldOffset;
  }

  public static boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
    try {
      return (boolean) COMPARE_AND_SET_LONG.invoke(UNSAFE, o, offset, expected, x);
    } catch (Throwable ignore) {
      return false;
    }
  }

  public static boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
    try {
      return (boolean) COMPARE_AND_SET_INT.invoke(UNSAFE, o, offset, expected, x);
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  public static long objectFieldOffset(Field f) {
    try {
      return (long) OBJECT_FIELD_OFFSET.invoke(UNSAFE, f);
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  public static void main(String[] args) {
    LongAdder adder = new LongAdder();
    adder.increment();
  }

}
