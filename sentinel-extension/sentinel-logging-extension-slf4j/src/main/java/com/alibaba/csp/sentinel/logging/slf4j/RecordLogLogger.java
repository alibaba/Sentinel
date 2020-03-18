package com.alibaba.csp.sentinel.logging.slf4j;

import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.Logger;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.slf4j.LoggerFactory;

/**
 * @author wavesZh
 */
@LogTarget(RecordLog.LOGGER_NAME)
public class RecordLogLogger implements Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(RecordLog.LOGGER_NAME);

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        logger.trace(msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

}
