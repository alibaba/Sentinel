package com.taobao.middleware.logger.log4j2;

import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.option.Log4j2ActivateOption;
import com.taobao.middleware.logger.support.LoggerHelper;
import com.taobao.middleware.logger.support.LoggerSupport;
import com.taobao.middleware.logger.util.MessageUtil;

/**
 * @author zhuyong on 2017/4/13.
 */
public class Log4j2Logger extends LoggerSupport implements Logger {

    private org.apache.logging.log4j.Logger delegate;

    public Log4j2Logger(org.apache.logging.log4j.Logger delegate) {
        super(delegate);

        if (delegate == null) {
            throw new IllegalArgumentException("delegate Logger is null");
        }
        this.delegate = delegate;

        this.activateOption = new Log4j2ActivateOption(delegate);
    }

    @Override
    public void debug(String context, String message) {
        if (isDebugEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.debug(MessageUtil.getMessage(context, message));
        }
    }

    @Override
    public void debug(String context, String format, Object... args) {
        if (isDebugEnabled()) {
            format = LoggerHelper.getResourceBundleString(getProductName(), format);
            delegate.debug(MessageUtil.getMessage(context, MessageUtil.formatMessage(format, args)));
        }
    }

    @Override
    public void info(String context, String message) {
        if (isInfoEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.info(MessageUtil.getMessage(context, message));
        }
    }

    @Override
    public void info(String context, String format, Object... args) {
        if (isInfoEnabled()) {
            format = LoggerHelper.getResourceBundleString(getProductName(), format);
            delegate.info(MessageUtil.getMessage(context, MessageUtil.formatMessage(format, args)));
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.warn(MessageUtil.getMessage(null, message), t);
        }
    }

    @Override
    public void warn(String context, String message) {
        if (isWarnEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.warn(MessageUtil.getMessage(context, message));
        }
    }

    @Override
    public void warn(String context, String format, Object... args) {
        if (isWarnEnabled()) {
            format = LoggerHelper.getResourceBundleString(getProductName(), format);
            delegate.warn(MessageUtil.getMessage(context, MessageUtil.formatMessage(format, args)));
        }
    }

    @Override
    public void error(String context, String errorCode, String message) {
        if (isErrorEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.error(MessageUtil.getMessage(context, errorCode, message));
        }
    }

    @Override
    public void error(String context, String errorCode, String message, Throwable t) {
        if (isErrorEnabled()) {
            message = LoggerHelper.getResourceBundleString(getProductName(), message);
            delegate.error(MessageUtil.getMessage(context, errorCode, message), t);
        }
    }

    @Override
    public void error(String context, String errorCode, String format, Object... args) {
        if (isErrorEnabled()) {
            format = LoggerHelper.getResourceBundleString(getProductName(), format);
            delegate.error(MessageUtil.getMessage(context, errorCode, MessageUtil.formatMessage(format, args)));
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }
}
