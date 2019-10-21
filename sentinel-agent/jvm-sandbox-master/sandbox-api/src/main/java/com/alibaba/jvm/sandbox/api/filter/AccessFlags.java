package com.alibaba.jvm.sandbox.api.filter;

/**
 * Access标记体系
 * <p>
 * 用于修饰{@link Filter#doClassFilter(int, String, String, String[], String[])}和
 * {@link Filter#doMethodFilter(int, String, String[], String[], String[])}的access
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class AccessFlags {

    private static final int BASE = 0x01;

    public static final int ACF_PUBLIC = BASE << 0;
    public static final int ACF_PRIVATE = BASE << 1;
    public static final int ACF_PROTECTED = BASE << 2;
    public static final int ACF_STATIC = BASE << 3;
    public static final int ACF_FINAL = BASE << 4;
    public static final int ACF_INTERFACE = BASE << 5;
    public static final int ACF_NATIVE = BASE << 6;
    public static final int ACF_ABSTRACT = BASE << 7;
    public static final int ACF_ENUM = BASE << 8;
    public static final int ACF_ANNOTATION = BASE << 9;

    private final int accessFlag;

    public AccessFlags(int accessFlag) {
        this.accessFlag = accessFlag;
    }

    public int getAccessFlag() {
        return accessFlag;
    }

}
