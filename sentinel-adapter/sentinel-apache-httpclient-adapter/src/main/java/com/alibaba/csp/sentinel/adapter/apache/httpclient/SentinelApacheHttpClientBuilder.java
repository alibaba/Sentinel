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
package com.alibaba.csp.sentinel.adapter.apache.httpclient;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.ClientExecChain;

import java.io.IOException;

/**
 * @author zhaoyuguang
 */
public class SentinelApacheHttpClientBuilder extends HttpClientBuilder {

    private final SentinelApacheHttpClientConfig config;

    public SentinelApacheHttpClientBuilder(){
        this.config = new SentinelApacheHttpClientConfig();
    }

    public SentinelApacheHttpClientBuilder(SentinelApacheHttpClientConfig config){
        this.config = config;
    }

    @Override
    protected ClientExecChain decorateMainExec(final ClientExecChain mainExec) {
        return new ClientExecChain() {
            @Override
            public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request,
                                                 HttpClientContext clientContext, HttpExecutionAware execAware)
                    throws IOException, HttpException {
                Entry entry = null;
                try {
                    String name = config.getExtractor().extractor(request);
                    if (!StringUtil.isEmpty(config.getPrefix())) {
                        name = config.getPrefix() + name;
                    }
                    entry = SphU.entry(name, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
                    return mainExec.execute(route, request, clientContext, execAware);
                } catch (BlockException e) {
                    return config.getFallback().handle(request, e);
                } catch (Throwable t) {
                    Tracer.traceEntry(t, entry);
                    throw t;
                } finally {
                    if (entry != null) {
                        entry.exit();
                    }
                }
            }
        };
    }
}
