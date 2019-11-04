/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.spi;

import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.config.SentinelConfig;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class ServiceLoaderUtil {

    private static final String CLASSLOADER_DEFAULT = "default";
    private static final String CLASSLOADER_CONTEXT = "context";

    public static <S> ServiceLoader<S> getServiceLoader(Class<S> clazz) {
        if (shouldUseContextClassloader()) {
            return ServiceLoader.load(clazz);
        } else {
            return ServiceLoader.load(clazz, clazz.getClassLoader());
        }
    }

    public static boolean shouldUseContextClassloader() {
        String classloaderConf = SentinelConfig.getConfig(SentinelConfig.SPI_CLASSLOADER);
        return CLASSLOADER_CONTEXT.equalsIgnoreCase(classloaderConf);
    }

    private ServiceLoaderUtil() {}
}
