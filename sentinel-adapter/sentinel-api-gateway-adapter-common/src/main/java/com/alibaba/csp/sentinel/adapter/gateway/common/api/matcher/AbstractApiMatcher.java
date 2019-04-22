/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.api.matcher;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public abstract class AbstractApiMatcher<T> implements Predicate<T> {

    protected final String apiName;
    protected final ApiDefinition apiDefinition;
    /**
     * We use {@link com.alibaba.csp.sentinel.util.function.Predicate} here as the min JDK version is 1.7.
     */
    protected final Set<Predicate<T>> matchers = new HashSet<>();

    public AbstractApiMatcher(ApiDefinition apiDefinition) {
        AssertUtil.notNull(apiDefinition, "apiDefinition cannot be null");
        AssertUtil.assertNotBlank(apiDefinition.getApiName(), "apiName cannot be empty");
        this.apiName = apiDefinition.getApiName();
        this.apiDefinition = apiDefinition;

        try {
            initializeMatchers();
        } catch (Exception ex) {
            RecordLog.warn("[GatewayApiMatcher] Failed to initialize internal matchers", ex);
        }
    }

    /**
     * Initialize the matchers.
     */
    protected abstract void initializeMatchers();

    @Override
    public boolean test(T t) {
        for (Predicate<T> matcher : matchers) {
            if (matcher.test(t)) {
                return true;
            }
        }
        return false;
    }

    public String getApiName() {
        return apiName;
    }

    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }
}
