/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.util.ReflectUtil;

/**
 * @author zhangxn8
 */
public class MotanUtils {

    private MotanUtils() {}

    public static String getMethodResourceName(Caller<?> caller, Request request){
        return getMethodResourceName(caller, request, false);
    }

    public static String getMethodResourceName(Caller<?> caller, Request request, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        String interfaceResource = useGroupAndVersion ? caller.getUrl().getPath(): caller.getInterface().getName();
        buf.append(interfaceResource)
                .append(":")
                .append(request.getMethodName())
                .append("(");
        boolean isFirst = true;
        try {
            Class<?>[] classTypes = ReflectUtil.forNames(request.getParamtersDesc());
            for (Class<?> clazz : classTypes) {
                if (!isFirst) {
                    buf.append(",");
                }
                buf.append(clazz.getName());
                isFirst = false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        buf.append(")");
        return buf.toString();
    }

    public static String getMethodResourceName(Caller<?> caller, Request request, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getMethodResourceName(caller, request,MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getMethodResourceName(caller, request,MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled());
        }
    }

    public static String getInterfaceName(Caller<?> caller) {
        return getInterfaceName(caller, false);
    }

    public static String getInterfaceName(Caller<?> caller, Boolean useGroupAndVersion) {
        return useGroupAndVersion ? caller.getUrl().getApplication() : caller.getInterface().getName();
    }

    public static String getInterfaceName(Caller<?> caller, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getInterfaceName(caller, MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getInterfaceName(caller, MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled());
        }
    }

}
