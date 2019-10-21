package com.taobao.middleware.logger.option;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.LogbackException;

public class LogbackLoggerContextUtil {

    private static LoggerContext loggerContext = null;

    public static LoggerContext getLoggerContext() {
        if (loggerContext == null) {
            ILoggerFactory lcObject = LoggerFactory.getILoggerFactory();

            if (!(lcObject instanceof LoggerContext)) {
                throw new LogbackException(
                                           "Expected LOGBACK binding with SLF4J, but another log system has taken the place: "
                                                   + lcObject.getClass().getSimpleName());
            }

            loggerContext = (LoggerContext) lcObject;
        }

        return loggerContext;
    }
}
