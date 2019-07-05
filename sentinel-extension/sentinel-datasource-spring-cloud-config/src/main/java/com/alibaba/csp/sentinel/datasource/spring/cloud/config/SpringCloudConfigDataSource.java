/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.datasource.spring.cloud.config;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class SpringCloudConfigDataSource<T> extends AbstractDataSource<String, T> {


    private ScheduledExecutorService poolExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("sentinel-spring-application-ds-update"));

    private PropertySourceLocator sourceLocator;

    private Environment environment;

    private String property;


    public SpringCloudConfigDataSource(final PropertySourceLocator sourceLocator,
                                       final Environment environment, final String property, Converter<String, T> converter) {
        super(converter);
        if (sourceLocator == null || environment == null
                || StringUtil.isBlank(property)) {
            throw new IllegalArgumentException(String.format("Bad argument: sourceLocator=[%s], environment=[%s], property=[%s]", sourceLocator, environment, property));
        }
        this.sourceLocator = sourceLocator;
        this.environment = environment;
        this.property = property;
        loadInitialConfig();
        poolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                loadInitialConfig();
            }
        }, 3, 5, TimeUnit.SECONDS);

    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[SpringCloudConfigDataSource] WARN: initial application is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[SpringCloudConfigDataSource] Error when loading initial application", ex);
        }
    }

    @Override
    public String readSource() {
        PropertySource<?> locate = sourceLocator.locate(environment);
        if (locate != null) {
            Object value = locate.getProperty(this.property);
            if (value instanceof String) {
                return value.toString();
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (poolExecutor != null) {
            poolExecutor.shutdown();
        }
    }


}
