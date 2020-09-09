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

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Eric Zhao
 */
public final class DubboUtils {

    public static final String SENTINEL_DUBBO_APPLICATION_KEY = "dubboApplication";

    public static String getApplication(Invocation invocation, String defaultValue) {
        if (invocation == null || invocation.getAttachments() == null) {
            throw new IllegalArgumentException("Bad invocation instance");
        }
        return invocation.getAttachment(SENTINEL_DUBBO_APPLICATION_KEY, defaultValue);
    }

    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation){
        return getMethodResourceName(invoker, invocation, false);
    }

    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        String interfaceResource = useGroupAndVersion ? invoker.getUrl().getColonSeparatedKey() : invoker.getInterface().getName();
        buf.append(interfaceResource)
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

    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getMethodResourceName(invoker, invocation, DubboAdapterGlobalConfig.getDubboInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getMethodResourceName(invoker, invocation, DubboAdapterGlobalConfig.getDubboInterfaceGroupAndVersionEnabled());
        }
    }


    public static String getInterfaceName(Invoker invoker) {
        return getInterfaceName(invoker, false);
    }

    public static String getInterfaceName(Invoker<?> invoker, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        return useGroupAndVersion ? invoker.getUrl().getColonSeparatedKey() : invoker.getInterface().getName();
    }

    public static String getInterfaceName(Invoker<?> invoker, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getInterfaceName(invoker, DubboAdapterGlobalConfig.getDubboInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getInterfaceName(invoker, DubboAdapterGlobalConfig.getDubboInterfaceGroupAndVersionEnabled());
        }
    }


    private DubboUtils() {
    }
}
