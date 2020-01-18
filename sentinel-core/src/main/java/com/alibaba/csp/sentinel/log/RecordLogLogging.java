package com.alibaba.csp.sentinel.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default logger implementation.
 * @author xue8
 */
public class RecordLogLogging extends LogBase implements com.alibaba.csp.sentinel.log.Logger {
    private static final Logger heliumRecordLog = Logger.getLogger("cspSentinelRecordLog");
    private static final String FILE_NAME = "sentinel-record.log";
    private static Handler logHandler = null;

    static {
        logHandler = makeLogger(FILE_NAME, heliumRecordLog);
    }

    @Override
    public void info(String detail, Object... params) {
        log(heliumRecordLog, logHandler, Level.INFO, detail, params);
    }

    @Override
    public void info(String detail, Throwable e) {
        log(heliumRecordLog, logHandler, Level.INFO, detail, e);
    }

    @Override
    public void warn(String detail, Object... params) {
        log(heliumRecordLog, logHandler, Level.WARNING, detail, params);
    }

    @Override
    public void warn(String detail, Throwable e) {
        log(heliumRecordLog, logHandler, Level.WARNING, detail, e);
    }
}
