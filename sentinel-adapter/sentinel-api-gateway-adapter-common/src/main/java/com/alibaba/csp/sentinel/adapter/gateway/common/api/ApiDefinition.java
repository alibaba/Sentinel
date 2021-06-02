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
package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import java.util.Objects;
import java.util.Set;

/**
 * A group of HTTP API patterns.
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public class ApiDefinition {

    private String apiName;
    private Set<ApiPredicateItem> predicateItems;

    public ApiDefinition() {}

    public ApiDefinition(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }

    public ApiDefinition setApiName(String apiName) {
        this.apiName = apiName;
        return this;
    }

    public Set<ApiPredicateItem> getPredicateItems() {
        return predicateItems;
    }

    public ApiDefinition setPredicateItems(Set<ApiPredicateItem> predicateItems) {
        this.predicateItems = predicateItems;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiDefinition that = (ApiDefinition)o;

        if (!Objects.equals(apiName, that.apiName)) { return false; }
        return Objects.equals(predicateItems, that.predicateItems);
    }

    @Override
    public int hashCode() {
        int result = apiName != null ? apiName.hashCode() : 0;
        result = 31 * result + (predicateItems != null ? predicateItems.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApiDefinition{" +
            "apiName='" + apiName + '\'' +
            ", predicateItems=" + predicateItems +
            '}';
    }
}
