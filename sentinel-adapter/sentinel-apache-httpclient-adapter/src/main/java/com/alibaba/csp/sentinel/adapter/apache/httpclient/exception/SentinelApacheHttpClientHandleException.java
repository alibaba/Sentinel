package com.alibaba.csp.sentinel.adapter.apache.httpclient.exception;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhaoyuguang
 */

public class SentinelApacheHttpClientHandleException {

    public static void handle(HttpContext context, Exception ex){
        AssertUtil.notNull(context, "context cannot be null");
        Entry entry = ((Entry) context.getAttribute(SentinelApacheHttpClientConfig.getAttributeName()));
        if (entry != null) {
            Tracer.traceEntry(ex, entry);
            entry.close();
            context.removeAttribute(SentinelApacheHttpClientConfig.getAttributeName());
        }
    }
}
