package com.taobao.middleware.logger;

import com.taobao.middleware.logger.log4j.Log4jLoggerFactory;
import com.taobao.middleware.logger.log4j2.Log4j2LoggerFactory;
import com.taobao.middleware.logger.nop.NopLoggerFactory;
import com.taobao.middleware.logger.slf4j.Slf4jLoggerFactory;
import com.taobao.middleware.logger.support.ILoggerFactory;
import com.taobao.middleware.logger.support.LogLog;
//import com.taobao.pandora.pandolet.service.PandoletService;
//
//import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 阿里中间件LoggerFactory，获取具体日志实现
 * 目前支持log4j/log4j2/slf4j/jcl日志门面和log4j/log4j2/logback日志实现：
 * log4j
 * log4j2
 * slf4j + logback
 * slf4j + slf4j-log4j12 + log4j
 * slf4j + slf4j-log4j-impl + log4j2
 * jcl + log4j
 * jcl + jcl-over-slf4j + slf4j + logback
 * jcl + jcl-over-slf4j + slf4j + slf4j-log4j-impl + log4j
 * 查找实现的优先顺序依次为slf4j > log4j > log4j2
 * </pre>
 *
 * @author zhuyong 2014年3月20日 上午10:17:33
 */
public class LoggerFactory {

    private LoggerFactory() {
    }

    private static volatile ILoggerFactory LOGGER_FACTORY;
    private static Map<String, Logger> loggerCache;

    // 查找常用的日志框架
    static {
        try {
            setLoggerFactory(new Slf4jLoggerFactory());
            LogLog.info("Init JM logger with Slf4jLoggerFactory success, " + LoggerFactory.class.getClassLoader());
        } catch (Throwable e1) {
            try {
                setLoggerFactory(new Log4jLoggerFactory());
                LogLog.info("Init JM logger with Log4jLoggerFactory, " + LoggerFactory.class.getClassLoader());
            } catch (Throwable e2) {
                try {
                    setLoggerFactory(new Log4j2LoggerFactory());
                    LogLog.info("Init JM logger with Log4j2LoggerFactory, " + LoggerFactory.class.getClassLoader());
                } catch (Throwable e3) {
                    setLoggerFactory(new NopLoggerFactory());
                    LogLog.warn("Init JM logger with NopLoggerFactory, pay attention. "
                                + LoggerFactory.class.getClassLoader(), e2);
                }
            }
        }

        loggerCache = new ConcurrentHashMap<String, Logger>();
    }

    public static Logger getLogger(String name) {
        Logger logger = loggerCache.get(name);
        if (logger == null) {
            synchronized (LOGGER_FACTORY) {
                logger = loggerCache.get(name);
                if (logger == null) {
                    logger = LOGGER_FACTORY.getLogger(name);
                    loggerCache.put(name, logger);
                }
            }
        }
        return logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private static void setLoggerFactory(ILoggerFactory loggerFactory) {
        if (loggerFactory != null) {
            LoggerFactory.LOGGER_FACTORY = loggerFactory;
        }
    }
}
