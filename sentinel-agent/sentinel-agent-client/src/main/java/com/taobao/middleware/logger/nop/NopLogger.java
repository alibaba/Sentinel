package com.taobao.middleware.logger.nop;

import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.support.LoggerSupport;

public class NopLogger extends LoggerSupport implements Logger {

    public NopLogger(){
        super(null);
    }

    @Override
    public void debug(String context, String message) {

    }

    @Override
    public void debug(String context, String format, Object... args) {

    }

    @Override
    public void info(String context, String message) {

    }

    @Override
    public void info(String context, String format, Object... args) {

    }

    public void warn(String message, Throwable t) {

    }

    @Override
    public void warn(String context, String message) {

    }

    @Override
    public void warn(String context, String format, Object... args) {

    }

    @Override
    public void error(String context, String errorCode, String message) {

    }

    @Override
    public void error(String context, String errorCode, String message, Throwable t) {

    }

    @Override
    public void error(String context, String errorCode, String format, Object... args) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }
}
