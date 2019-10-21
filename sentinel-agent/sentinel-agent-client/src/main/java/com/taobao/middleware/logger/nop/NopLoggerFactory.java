package com.taobao.middleware.logger.nop;

import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.support.ILoggerFactory;

public class NopLoggerFactory implements ILoggerFactory {

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new NopLogger();
    }

    @Override
    public Logger getLogger(String name) {
        return new NopLogger();
    }
}
