package com.alibaba.csp.sentinel.adapter.springhttpclient;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.springhttpclient.fallback.SpringHttpClientFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author jyy
 */
public class SentinelClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Entry entry = null;
        try {
            String resourceName = getResourceName(request);
            if (!StringUtil.isEmpty(resourceName)) {
                entry = SphU.entry(resourceName, EntryType.OUT);
            }
            return execution.execute(request,body);
        } catch (BlockException e) {
            return SpringHttpClientFallbackRegistry.getHttpClientFallbac().handle(request,body,e);
        } catch (IOException  | RuntimeException e2) {
            Tracer.traceEntry(e2, entry);
            throw e2;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
    private String getResourceName(HttpRequest request) {
       return ResourceNameUtil.getResourceName(request.getURI());
    }
}
