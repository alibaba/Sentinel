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
package com.alibaba.csp.sentinel.adapter.mybatis;


import com.alibaba.csp.sentinel.adapter.mybatis.callback.ResourceNameCleaner;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @author kaizi2009
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ),
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        )
})
public class SentinelMapperInterceptor extends AbstractSentinelInterceptor {
    private ResourceNameCleaner resourceNameCleaner;

    public SentinelMapperInterceptor() {
        super();
    }

    public SentinelMapperInterceptor(ResourceNameCleaner resourceNameCleaner) {
        super();
        this.resourceNameCleaner = resourceNameCleaner;
    }

    @Override
    protected String getResourceName(MappedStatement mappedStatement) {
        String resourceName = mappedStatement.getId();
        if (getResourceNameCleaner() != null) {
            resourceName = getResourceNameCleaner().clean(resourceName);
        }
        return resourceName;
    }

    public SentinelMapperInterceptor setResourceNameCleaner(ResourceNameCleaner resourceNameCleaner) {
        this.resourceNameCleaner = resourceNameCleaner;
        return this;
    }

    public ResourceNameCleaner getResourceNameCleaner() {
        return resourceNameCleaner;
    }
}
