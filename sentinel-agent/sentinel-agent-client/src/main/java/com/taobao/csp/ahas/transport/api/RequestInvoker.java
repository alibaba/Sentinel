package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.ahas.gw.client.api.bootstrap.ResponseCallback;

public interface RequestInvoker {
   <R> Response<R> invoke(RequestUri var1, Request var2, Class<?> var3) throws RequestException;

   void asyncInvoke(RequestUri var1, Request var2, ResponseCallback var3) throws RequestException;
}
