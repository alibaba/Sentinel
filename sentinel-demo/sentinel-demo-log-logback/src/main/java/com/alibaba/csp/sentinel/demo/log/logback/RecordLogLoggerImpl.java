package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.LogType;
import com.alibaba.csp.sentinel.log.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a demo shows how to create a customized logger implementation.
 *
 * <ul>
 * <li>1. Create a class which implements the {@link Logger} SPI interface</li>
 * <li>2. Use a {@link LogTarget} to specify the log type</li>
 * <li>3. Implement your own method </li>
 * <li>4. Add your logger in {@code com.alibaba.csp.sentinel.log.Logger} file which is stored in
 * {@code resources/META-INF/services/} directory </li>
 * </ul>
 *
 * @author xue8
 */
@LogTarget(value = LogType.RECORD_LOG)
public class RecordLogLoggerImpl implements Logger {
    org.slf4j.Logger logger = LoggerFactory.getLogger("recordLogLogger");

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
