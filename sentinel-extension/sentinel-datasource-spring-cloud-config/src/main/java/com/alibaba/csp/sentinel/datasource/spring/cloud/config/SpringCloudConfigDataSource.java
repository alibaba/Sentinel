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

/**
 * ${@link SpringCloudConfigDataSource} A read-only {@code DataSource} with spring-cloud-config backend through
 * ${@link SentinelRuleStorage} retrieve the spring-cloud-config data
 * When the data in backend has been modified, if {@code DataSource} want to read the latest data.
 * The backend should notice spring-cloud-client invoke the ${@link SentinelRuleLocator#refresh()}
 * to update ${@link SentinelRuleStorage} data. Web hook may be a good notice way.
 *
 * @author lianglin
 * @since 1.7.0
 */
public class SpringCloudConfigDataSource<T> extends AbstractDataSource<String, T> {


    private String ruleKey;


    public SpringCloudConfigDataSource(final String ruleKey, Converter<String, T> converter) {
        super(converter);
        if (StringUtil.isBlank(ruleKey)) {
            throw new IllegalArgumentException(String.format("Bad argument: ruleKey=[%s]", ruleKey));
        }

        this.ruleKey = ruleKey;
        loadInitialConfig();
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
        return SentinelRuleStorage.retrieveRule(ruleKey);
    }

    @Override
    public void close() throws Exception {

    }


}
