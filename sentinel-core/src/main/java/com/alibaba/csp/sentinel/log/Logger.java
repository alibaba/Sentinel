package com.alibaba.csp.sentinel.log;

/**
 * Provide logger SPI interface
 * @author xue8
 */
public interface Logger {
    void info(String detail, Object... params);

    void info(String detail, Throwable e);

    void warn(String detail, Object... params);

    void warn(String detail, Throwable e);
}
