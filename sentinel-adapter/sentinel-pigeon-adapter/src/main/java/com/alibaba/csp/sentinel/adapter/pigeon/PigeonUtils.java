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
package com.alibaba.csp.sentinel.adapter.pigeon;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

import java.lang.reflect.Parameter;

public final class PigeonUtils {

    public static String getResourceName(InvocationContext context) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(context.getRequest().getServiceName())
                .append(":")
                .append(context.getRequest().getMethodName())
                .append("(");
        boolean isFirst = true;
        for (String clazz : context.getRequest().getParamClassName()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz);
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    public static Parameter[] getMethodArguments(ProviderContext providerContext) {
        return providerContext.getServiceMethod().getMethod().getParameters();
    }

    private PigeonUtils() {}

}
