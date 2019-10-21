package com.taobao.middleware.logger.log4j2;

import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.log4j.Log4jLogger;
import com.taobao.middleware.logger.nop.NopLogger;
import com.taobao.middleware.logger.support.ILoggerFactory;
import com.taobao.middleware.logger.support.LogLog;
import org.apache.logging.log4j.LogManager;

/**
 * @author zhuyong on 2017/4/13.
 */
public class Log4j2LoggerFactory implements ILoggerFactory {

    public Log4j2LoggerFactory() throws ClassNotFoundException {
        Class.forName("org.apache.logging.log4j.core.Logger");
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        try {
            return new Log4j2Logger(LogManager.getLogger(clazz));
        } catch (Throwable t) {
            LogLog.error("Failed to get Log4j2Logger", t);
            return new NopLogger();
        }
    }

    @Override
    public Logger getLogger(String name) {
        try {
            return new Log4j2Logger(LogManager.getLogger(name));
        } catch (Throwable t) {
            LogLog.error("Failed to get Log4j2Logger", t);
            return new NopLogger();
        }
    }
}
