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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.RestClientFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * {@link ClientHttpRequestInterceptor} for integrating Sentinel with Spring's
 * {@link org.springframework.web.client.RestClient}.
 *
 * <p>This interceptor creates two levels of Sentinel resources for each request:
 * <ul>
 * <li><b>Host-level resource</b>: {@code METHOD:scheme://host[:port]},
 * e.g. {@code GET:https://httpbin.org}</li>
 * <li><b>Path-level resource</b>: extracted by {@link RestClientResourceExtractor},
 * by default: {@code METHOD:scheme://host[:port]/path},
 * e.g. {@code GET:https://httpbin.org/get}</li>
 * </ul>
 *
 * <p>This dual-level design allows:
 * <ul>
 * <li>Host-level flow control for overall traffic to a service</li>
 * <li>Path-level flow control for specific endpoints</li>
 * <li>Circuit breaking at either level</li>
 * </ul>
 *
 * <p>Supports:
 * <ul>
 * <li>Flow control (QPS limiting)</li>
 * <li>Circuit breaking (degrade)</li>
 * <li>Custom resource name extraction via {@link RestClientResourceExtractor}</li>
 * <li>Custom fallback responses via {@link RestClientFallback}</li>
 * </ul>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClientConfig
 * @see RestClientResourceExtractor
 * @see RestClientFallback
 */
public class SentinelRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final SentinelRestClientConfig config;

    public SentinelRestClientInterceptor() {
        this.config = new SentinelRestClientConfig();
    }

    public SentinelRestClientInterceptor(SentinelRestClientConfig config) {
        AssertUtil.notNull(config, "config cannot be null");
        this.config = config;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        
        String hostResource = buildHostResourceName(request, uri);
        String pathResource = buildPathResourceName(request);
        
        boolean entryWithPath = !hostResource.equals(pathResource);

        Entry hostEntry = null;
        Entry pathEntry = null;
        
        try {
            hostEntry = SphU.entry(hostResource, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            
            if (entryWithPath) {
                pathEntry = SphU.entry(pathResource, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            }

            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode().is5xxServerError()) {
                RuntimeException ex = new RuntimeException("Server error: " + response.getStatusCode().value());
                Tracer.traceEntry(ex, hostEntry);
                if (pathEntry != null) {
                    Tracer.traceEntry(ex, pathEntry);
                }
            }

            return response;
        } catch (BlockException ex) {
            return handleBlockException(request, body, execution, ex);
        } catch (IOException ex) {
            // Path entry does not need to be traced if an IO exception occurred.
            Tracer.traceEntry(ex, hostEntry);
            throw ex;
        } finally {
            if (pathEntry != null) {
                pathEntry.exit();
            }
            if (hostEntry != null) {
                hostEntry.exit();
            }
        }
    }

    private String buildHostResourceName(HttpRequest request, URI uri) {
        String hostResource = request.getMethod().toString() + ":" + 
                              uri.getScheme() + "://" + 
                              uri.getHost() + 
                              (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        
        if (StringUtil.isNotBlank(config.getResourcePrefix())) {
            hostResource = config.getResourcePrefix() + hostResource;
        }
        
        return hostResource;
    }

    private String buildPathResourceName(HttpRequest request) {
        String pathResource = config.getResourceExtractor().extract(request);
        
        if (StringUtil.isNotBlank(config.getResourcePrefix())) {
            pathResource = config.getResourcePrefix() + pathResource;
        }
        
        return pathResource;
    }

    private ClientHttpResponse handleBlockException(HttpRequest request, byte[] body,
                                                    ClientHttpRequestExecution execution, 
                                                    BlockException ex) {
		return config.getFallback().handle(request, body, execution, ex);
	}
}