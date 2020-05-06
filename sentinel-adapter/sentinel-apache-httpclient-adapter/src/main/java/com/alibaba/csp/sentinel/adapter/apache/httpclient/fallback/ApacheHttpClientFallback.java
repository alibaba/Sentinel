package com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * @author zhaoyuguang
 */

public interface ApacheHttpClientFallback {

    void handle(HttpRequest request, HttpContext context, BlockException e) throws HttpException, IOException;
}
