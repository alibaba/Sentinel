package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.LogType;
import com.alibaba.csp.sentinel.log.Logger;
import org.slf4j.LoggerFactory;

@LogTarget(value = LogType.COMMAND_CENTER_LOG)
public class CommandCenterLogLoggerImpl implements Logger {
    org.slf4j.Logger logger = LoggerFactory.getLogger("commandCenterLogLogger");

    public void info(String detail, Object... params) {
        logger.info(detail, params);
    }

    public void info(String detail, Throwable e) {
        logger.info(detail, e);
    }

    public void warn(String detail, Object... params) {
        logger.warn(detail, params);
    }

    public void warn(String detail, Throwable e) {
        logger.warn(detail, e);
    }

}
