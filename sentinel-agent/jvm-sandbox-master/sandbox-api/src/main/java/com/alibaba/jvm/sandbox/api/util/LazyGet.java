package com.alibaba.jvm.sandbox.api.util;

/**
 * 懒加载
 *
 * @param <T> 懒加载类型
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.2.2}
 */
public abstract class LazyGet<T> {

    private volatile boolean isInit = false;
    private volatile T object;

    abstract protected T initialValue() throws Throwable;

    public T get() {

        if (isInit) {
            return object;
        }

        // lazy get
        try {
            object = initialValue();
            isInit = true;
            return object;
        } catch (Throwable throwable) {
            throw new LazyGetUnCaughtException(throwable);
        }

    }

    private static class LazyGetUnCaughtException extends RuntimeException {
        LazyGetUnCaughtException(Throwable cause) {
            super(cause);
        }
    }

}
