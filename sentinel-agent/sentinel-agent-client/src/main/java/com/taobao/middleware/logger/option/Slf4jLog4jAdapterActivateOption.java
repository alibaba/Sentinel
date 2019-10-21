package com.taobao.middleware.logger.option;

import java.lang.reflect.Field;

import com.taobao.middleware.logger.Logger;


/**
 * Slf4j-log4j12架构下的ActivateOption实现
 *
 * @author zhuyong 2014年3月20日 上午10:26:04
 */
public class Slf4jLog4jAdapterActivateOption extends Log4jActivateOption {

    private static Field loggerField = null;

    static {
        try {
          //  org.slf4j.
            loggerField = org.slf4j.impl.Log4jLoggerAdapter.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("logger must be instanceof org.slf4j.impl.Log4jLoggerAdapter", e);
        }
    }

    public Slf4jLog4jAdapterActivateOption(Object logger) {
        super(null);

        try {
            org.apache.log4j.Logger log4jLogger = (org.apache.log4j.Logger) loggerField.get(logger);
            super.logger = log4jLogger;
        } catch (Exception e) {
            throw new RuntimeException("logger must be instanceof org.slf4j.impl.Log4jLoggerAdapter", e);
        }
    }

    @Override
    public void activateAppender(Logger logger) {
        if (!(logger.getDelegate() instanceof org.slf4j.impl.Log4jLoggerAdapter)) {
            throw new IllegalArgumentException(
                    "logger must be org.slf4j.impl.Log4jLoggerAdapter, but it's "
                            + logger.getDelegate().getClass());
        }

        try {
            org.apache.log4j.Logger log4jLogger =
                    (org.apache.log4j.Logger) loggerField.get(logger.getDelegate());
            super.activateAppender(log4jLogger);
            setProductName(logger.getProductName());
        } catch (Exception e) {
            throw new RuntimeException("activateAppender error, ", e);
        }
    }
}
