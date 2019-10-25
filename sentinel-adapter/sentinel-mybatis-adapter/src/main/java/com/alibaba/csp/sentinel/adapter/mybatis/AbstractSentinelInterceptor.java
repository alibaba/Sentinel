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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.mybatis.callback.ResourceNameCleaner;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

/**
 * @author kaizi2009
 */
public abstract class AbstractSentinelInterceptor implements Interceptor {
    public static final String CONTEXT_NAME = "mybatis_context";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Entry entry = null;
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String resourceName = getResourceName(mappedStatement);
            if (StringUtil.isNotEmpty(resourceName)) {
                ContextUtil.enter(getContextName(), "");
                entry = SphU.entry(resourceName, EntryType.IN);
            }
            return invocation.proceed();
        } catch (BlockException e) {
            throw ExceptionFactory.wrapException(e.getMessage(), e);
        } catch (RuntimeException e1) {
            Tracer.traceEntry(e1, entry);
            throw e1;
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    protected String getContextName() {
        return CONTEXT_NAME;
    }

    /**
     * Get sentinel resource name
     * @param mappedStatement
     * @return resource name
     */
    abstract String getResourceName(MappedStatement mappedStatement);

}
