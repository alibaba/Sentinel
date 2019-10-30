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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * @author leyou
 */
abstract class AbstractDubboFilter implements Filter {

    protected String getResourceName(Invoker<?> invoker, Invocation invocation) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(invoker.getInterface().getName())
                .append(":")
                .append(invocation.getMethodName())
                .append("(");
        boolean isFirst = true;
        for (Class<?> clazz : invocation.getParameterTypes()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz.getName());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    protected String getResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {
        if (StringUtil.isBlank(prefix)) {
            return getResourceName(invoker, invocation);
        }
        StringBuilder buf = new StringBuilder(64);
        return buf.append(prefix)
                .append(getResourceName(invoker, invocation))
                .toString();


    }
}
