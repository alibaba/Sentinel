package com.alibaba.csp.sentinel.demo.zuul2.gateway.filters;

import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import org.apache.http.HttpStatus;

public class NotFoundEndpoint extends HttpSyncEndpoint {

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage response = new HttpResponseMessageImpl(request.getContext(), request, HttpStatus.SC_NOT_FOUND);
        response.finishBufferedBodyIfIncomplete();
        return response;
    }
}