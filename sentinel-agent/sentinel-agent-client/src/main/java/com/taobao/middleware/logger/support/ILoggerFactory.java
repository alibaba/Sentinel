package com.taobao.middleware.logger.support;

import com.taobao.middleware.logger.Logger;


public interface ILoggerFactory {

    Logger getLogger(Class<?> clazz);

    Logger getLogger(String name);
}
