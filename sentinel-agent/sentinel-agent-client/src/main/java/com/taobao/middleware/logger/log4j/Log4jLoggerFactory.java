package com.taobao.middleware.logger.log4j;

import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.nop.NopLogger;
import com.taobao.middleware.logger.support.ILoggerFactory;
import com.taobao.middleware.logger.support.LogLog;
import org.apache.log4j.LogManager;

public class Log4jLoggerFactory implements ILoggerFactory {

    public Log4jLoggerFactory() throws ClassNotFoundException {
        Class.forName("org.apache.log4j.Level");
    }

    public Logger getLogger(Class<?> clazz) {
        try {
            return new Log4jLogger(LogManager.getLogger(clazz));
        } catch (Throwable t) {
            LogLog.error("Failed to get Log4jLogger", t);
            return new NopLogger();
        }
    }

    public Logger getLogger(String name) {
        try {
            return new Log4jLogger(LogManager.getLogger(name));
        } catch (Throwable t) {
            LogLog.error("Failed to get Log4jLogger", t);
            return new NopLogger();
        }
    }
}
