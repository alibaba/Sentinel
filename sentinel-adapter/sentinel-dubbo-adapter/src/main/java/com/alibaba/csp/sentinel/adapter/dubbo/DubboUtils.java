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

import com.alibaba.dubbo.rpc.Invocation;

/**
 * @author Eric Zhao
 */
public final class DubboUtils {

    public static final String DUBBO_APPLICATION_KEY = "dubboApplication";

    public static String getApplication(Invocation invocation, String defaultValue) {
        if (invocation == null || invocation.getAttachments() == null) {
            throw new IllegalArgumentException("Bad invocation instance");
        }
        return invocation.getAttachment(DUBBO_APPLICATION_KEY, defaultValue);
    }

    private DubboUtils() {}
}
