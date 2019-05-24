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

import com.alibaba.csp.sentinel.adapter.pigeon.provider.DemoService;

import java.lang.reflect.Method;

public class MethodUtils {

    public static String buildResource(Method method) {
        String[] paramClazzName = new String[2];
        int i = 0;
        for (Class<?> clazz : method.getParameterTypes()) {
            paramClazzName[i] = clazz.getName();
            i ++;
        }

        StringBuilder buf = new StringBuilder(64);
        buf.append(DemoService.class.getName())
                .append(":")
                .append(method.getName())
                .append("(");
        boolean isFirst = true;
        for (String clazz : paramClazzName) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz);
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

}
