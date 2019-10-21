package com.taobao.middleware.logger.option;

import com.taobao.middleware.logger.Logger;

import java.lang.reflect.Field;

/**
 * @author zhuyong on 2017/4/18.
 */
public class Slf4jLog4j2AdapterActivateOption extends Log4j2ActivateOption {

    private static Field loggerField = null;

    static {
        try {
            loggerField = org.apache.logging.slf4j.Log4jLogger.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("logger must be instanceof org.apache.logging.slf4j.Log4jLogger", e);
        }
    }

    public Slf4jLog4j2AdapterActivateOption(Object logger) {
        super(null);

        try {
            org.apache.logging.log4j.core.Logger log4j2Logger = (org.apache.logging.log4j.core.Logger) loggerField.get(logger);
            super.logger = log4j2Logger;
            super.configuration = super.logger.getContext().getConfiguration();
        } catch (Exception e) {
            throw new RuntimeException("logger must be instanceof org.apache.logging.slf4j.Log4jLogger", e);
        }
    }

    @Override
    public void activateAppender(Logger logger) {
        if (!(logger.getDelegate() instanceof org.apache.logging.slf4j.Log4jLogger)) {
            throw new IllegalArgumentException(
                    "logger must be org.apache.logging.slf4j.Log4jLogger, but it's "
                            + logger.getDelegate().getClass());
        }

        try {
            org.apache.logging.log4j.core.Logger log4j2Logger = (org.apache.logging.log4j.core.Logger) loggerField.get(logger.getDelegate());
            super.activateAppender(log4j2Logger);
        } catch (Exception e) {
            throw new RuntimeException("activateAppender error, ", e);
        }
    }
}
