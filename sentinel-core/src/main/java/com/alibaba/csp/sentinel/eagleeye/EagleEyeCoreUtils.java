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
package com.alibaba.csp.sentinel.eagleeye;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

final class EagleEyeCoreUtils {

    public static final String EMPTY_STRING = "";
    public static final String NEWLINE = "\r\n";

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static String checkNotNullEmpty(String value, String name) throws IllegalArgumentException {
        if (isBlank(value)) {
            throw new IllegalArgumentException(name + " is null or empty");
        }
        return value;
    }

    public static <T> T checkNotNull(T value, String name) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(name + " is null");
        }
        return value;
    }

    public static <T> T defaultIfNull(T value, T defaultValue) {
        return (value == null) ? defaultValue : value;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String[] split(String str, char separatorChar) {
        return splitWorker(str, separatorChar, false);
    }

    private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static StringBuilder appendWithBlankCheck(String str, String defaultValue, StringBuilder appender) {
        if (isNotBlank(str)) {
            appender.append(str);
        } else {
            appender.append(defaultValue);
        }
        return appender;
    }

    public static StringBuilder appendWithNullCheck(Object obj, String defaultValue, StringBuilder appender) {
        if (obj != null) {
            appender.append(obj.toString());
        } else {
            appender.append(defaultValue);
        }
        return appender;
    }

    public static StringBuilder appendLog(String str, StringBuilder appender, char delimiter) {
        if (str != null) {
            int len = str.length();
            appender.ensureCapacity(appender.length() + len);
            for (int i = 0; i < len; i++) {
                char c = str.charAt(i);
                if (c == '\n' || c == '\r' || c == delimiter) {
                    c = ' ';
                }
                appender.append(c);
            }
        }
        return appender;
    }

    private static final ThreadLocal<FastDateFormat> dateFmt = new ThreadLocal<FastDateFormat>() {
        @Override
        protected FastDateFormat initialValue() {
            return new FastDateFormat();
        }
    };

    public static String formatTime(long timestamp) {
        return dateFmt.get().format(timestamp);
    }

    public static String getSystemProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (Throwable t) {
            return null;
        }
    }

    public static long getSystemPropertyForLong(String key, long defaultValue) {
        try {
            return Long.parseLong(System.getProperty(key));
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static boolean isHexNumeric(char ch) {
        return (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9');
    }

    public static boolean isNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static void shutdownThreadPool(ExecutorService pool, long awaitTimeMillis) {
        try {
            pool.shutdown();

            boolean done = false;
            if (awaitTimeMillis > 0) {
                try {
                    done = pool.awaitTermination(awaitTimeMillis, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                }
            }

            if (!done) {
                pool.shutdownNow();
            }
        } catch (Exception e) {
            // quietly
        }
    }

    // Unsafe mechanics
    @SuppressWarnings("restriction")
    private static final sun.misc.Unsafe UNSAFE = doGetUnsafe();

    @SuppressWarnings("restriction")
    public static sun.misc.Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Returns a sun.misc.Unsafe. Suitable for use in a 3rd party package.
     * Replace with a simple call to Unsafe.getUnsafe when integrating into a
     * jdk.
     *
     * @return a sun.misc.Unsafe
     */
    @SuppressWarnings("restriction")
    private static sun.misc.Unsafe doGetUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (Throwable tryReflectionInstead) {
        }
        try {
            return java.security.AccessController
                .doPrivileged(new java.security.PrivilegedExceptionAction<sun.misc.Unsafe>() {
                    @Override
                    public sun.misc.Unsafe run() throws Exception {
                        Class<sun.misc.Unsafe> k = sun.misc.Unsafe.class;
                        for (java.lang.reflect.Field f : k.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (k.isInstance(x)) { return k.cast(x); }
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    }
                });
        } catch (Throwable t) {
            return null;
        }
    }
}
