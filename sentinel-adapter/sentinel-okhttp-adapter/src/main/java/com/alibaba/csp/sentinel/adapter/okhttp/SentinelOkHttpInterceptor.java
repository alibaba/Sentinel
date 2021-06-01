/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.okhttp;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author zhaoyuguang
 */
public class SentinelOkHttpInterceptor implements Interceptor {

    private final SentinelOkHttpConfig config;

    public SentinelOkHttpInterceptor() {
        this.config = new SentinelOkHttpConfig();
    }

    public SentinelOkHttpInterceptor(SentinelOkHttpConfig config) {
        AssertUtil.notNull(config, "config cannot be null");
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Entry entry = null;
        try {
            Request request = chain.request();
            String name = config.getResourceExtractor().extract(request, chain.connection());
            if (StringUtil.isNotBlank(config.getResourcePrefix())) {
                name = config.getResourcePrefix() + name;
            }
            entry = SphU.entry(name, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return chain.proceed(request);
        } catch (BlockException e) {
            return config.getFallback().handle(chain.request(), chain.connection(), e);
        } catch (IOException ex) {
            Tracer.traceEntry(ex, entry);
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}