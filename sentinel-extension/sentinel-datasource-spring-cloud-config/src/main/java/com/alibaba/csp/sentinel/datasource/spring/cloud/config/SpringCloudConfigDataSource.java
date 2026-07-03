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

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>A read-only {@code DataSource} with Spring Cloud Config backend.</p>
 * <p>
 * It retrieves the Spring Cloud Config data stored in {@link SentinelRuleStorage}.
 * When the data in the backend has been modified, {@link SentinelRuleStorage} will
 * invoke {@link SpringCloudConfigDataSource#updateValues()} to update values dynamically.
 * </p>
 * <p>
 * To notify the client that the remote config has changed, users could bind a git
 * webhook callback with the {@link SentinelRuleLocator#refresh()} API.
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public class SpringCloudConfigDataSource<T> extends AbstractDataSource<String, T> {

    private final static Map<SpringCloudConfigDataSource, SpringConfigListener> listeners;

    static {
        listeners = new ConcurrentHashMap<>();
    }

    private final String ruleKey;

    public SpringCloudConfigDataSource(final String ruleKey, Converter<String, T> converter) {
        super(converter);
        if (StringUtil.isBlank(ruleKey)) {
            throw new IllegalArgumentException(String.format("Bad argument: ruleKey=[%s]", ruleKey));
        }

        this.ruleKey = ruleKey;
        loadInitialConfig();
        initListener();
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

    private void initListener() {
        listeners.put(this, new SpringConfigListener(this));
    }

    @Override
    public String readSource() {
        return SentinelRuleStorage.retrieveRule(ruleKey);
    }

    @Override
    public void close() throws Exception {
        listeners.remove(this);
    }

    public static void updateValues() {
        for (SpringConfigListener listener : listeners.values()) {
            listener.listenChanged();
        }
    }

    private static class SpringConfigListener {

        private SpringCloudConfigDataSource dataSource;

        public SpringConfigListener(SpringCloudConfigDataSource dataSource) {
            this.dataSource = dataSource;
        }

        public void listenChanged() {
            try {
                Object newValue = dataSource.loadConfig();
                dataSource.getProperty().updateValue(newValue);
            } catch (Exception e) {
                RecordLog.warn("[SpringConfigListener] load config error: ", e);
            }
        }
    }
}
