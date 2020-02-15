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
package com.alibaba.csp.sentinel.dubbo.springboot.utils;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.dubbo.common.URL;


public class ResourceUtils {
    private static String getResourceName(URL url, String methodName, Class<?>[] parameterTypes, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        String interfaceResource = useGroupAndVersion ? url.getColonSeparatedKey() : url.getServiceInterface();
        buf.append(interfaceResource)
                .append(":")
                .append(methodName)
                .append("(");
        boolean isFirst = true;
        for (Class<?> clazz : parameterTypes) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz.getName());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    public static String getResourceName(URL url, String methodName, Class<?>[] parameterTypes, Boolean useGroupAndVersion, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getResourceName(url, methodName, parameterTypes, useGroupAndVersion))
                    .toString();
        } else {
            return getResourceName(url, methodName, parameterTypes, useGroupAndVersion);
        }
    }


    public static String getInterfaceResourceName(URL url) {
        return DubboConfig.getDubboInterfaceGroupAndVersionEnabled() ? url.getColonSeparatedKey()
                : url.getServiceInterface();
    }
}
