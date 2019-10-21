package com.taobao.csp.ahas.transport.api;

public interface RequestInterceptor {
   void handle(Request var1) throws RequestException;

   void invoke(Request var1) throws RequestException;
}
