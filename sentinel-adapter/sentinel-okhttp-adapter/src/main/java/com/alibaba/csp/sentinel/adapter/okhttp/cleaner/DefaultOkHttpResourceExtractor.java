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
package com.alibaba.csp.sentinel.adapter.okhttp.cleaner;

import com.alibaba.csp.sentinel.adapter.okhttp.config.SentinelOkHttpConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import okhttp3.Connection;
import okhttp3.Request;

/**
 * @author zhaoyuguang
 */
public class DefaultOkHttpResourceExtractor implements OkHttpResourceExtractor {

    @Override
    public String extract(Request request, Connection connection) {
        StringBuilder buf = new StringBuilder(64);
        if (!StringUtil.isEmpty(SentinelOkHttpConfig.getPrefix())) {
            buf.append(SentinelOkHttpConfig.getPrefix());
        }
        buf.append(request.method()).append(":").append(request.url().toString());
        return buf.toString();
    }
}
