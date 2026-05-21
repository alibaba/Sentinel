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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;

/**
 * Apache HttpClient 5.x adapter for Sentinel.
 *
 * <p>This handler implements {@link ExecChainHandler} to intercept outgoing HTTP requests
 * and protect them with Sentinel flow control.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * CloseableHttpClient httpclient = HttpClients.custom()
 *     .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
 *         new SentinelApacheHttpClient5Handler())
 *     .build();
 * }</pre>
 *
 * @author uuuyuqi
 */
public class SentinelApacheHttpClient5Handler implements ExecChainHandler {

    private final SentinelApacheHttpClientConfig config;

    public SentinelApacheHttpClient5Handler() {
        this.config = new SentinelApacheHttpClientConfig();
    }

    public SentinelApacheHttpClient5Handler(SentinelApacheHttpClientConfig config) {
        this.config = config;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope,
                                       ExecChain execChain) throws IOException, HttpException {
        String name = config.getExtractor().extractor(classicHttpRequest);
        if (StringUtil.isEmpty(name)) {
            return execChain.proceed(classicHttpRequest, scope);
        }

        if (StringUtil.isNotEmpty(config.getPrefix())) {
            name = config.getPrefix() + name;
        }

        Entry entry = null;
        try {
            entry = SphU.entry(name, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return execChain.proceed(classicHttpRequest, scope);
        } catch (BlockException e) {
            return config.getFallback().handle(classicHttpRequest, e);
        } catch (IOException | HttpException | RuntimeException e) {
            Tracer.traceEntry(e, entry);
            throw e;
        } catch (Throwable t) {
            Tracer.traceEntry(t, entry);
            throw new RuntimeException(t);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
