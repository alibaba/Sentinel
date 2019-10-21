package com.taobao.middleware.logger.slf4j;


import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.nop.NopLogger;
import com.taobao.middleware.logger.support.ILoggerFactory;
import com.taobao.middleware.logger.support.LogLog;

public class Slf4jLoggerFactory implements ILoggerFactory {

    public Slf4jLoggerFactory() throws ClassNotFoundException {
        Class.forName("org.slf4j.impl.StaticLoggerBinder");
    }

    public Logger getLogger(String name) {
        try {
            return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(name));
        } catch (Throwable t) {
            LogLog.error("Failed to get Slf4jLogger", t);
            return new NopLogger();
        }
    }

    public Logger getLogger(Class<?> clazz) {
        try {
            return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(clazz));
        } catch (Throwable t) {
            LogLog.error("Failed to get Slf4jLogger", t);
            return new NopLogger();
        }
    }
}
