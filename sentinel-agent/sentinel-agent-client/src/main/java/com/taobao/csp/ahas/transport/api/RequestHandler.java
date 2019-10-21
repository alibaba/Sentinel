package com.taobao.csp.ahas.transport.api;

public interface RequestHandler {
   <R> Response<R> handle(Request var1) throws RequestException;
}
