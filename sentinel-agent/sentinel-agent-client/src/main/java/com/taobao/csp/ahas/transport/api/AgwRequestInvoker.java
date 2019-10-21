package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.ahas.gw.client.api.bootstrap.ResponseCallback;
import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;

public abstract class AgwRequestInvoker implements RequestInvoker {
   private RequestInterceptor requestInterceptor;

   public <R> Response<R> invoke(RequestUri uri, Request request, Class<?> clazz) throws RequestException {
      if (this.requestInterceptor != null) {
         this.requestInterceptor.invoke(request);
      }

      AgwRequestUri requestUri = (AgwRequestUri)uri;
      request.addHeader("rid", requestUri.getRequestId());
      String json = (new JsonEncoder()).encode(request);

      String result;
      try {
         result = this.doInvoke(requestUri, json);
      } catch (AgwRpcException var8) {
         throw var8;
      }

      return (new ResponseJsonDecoder(clazz)).decode(result);
   }

   public void asyncInvoke(RequestUri uri, Request request, ResponseCallback callback) throws RequestException {
      if (this.requestInterceptor != null) {
         this.requestInterceptor.invoke(request);
      }

      AgwRequestUri requestUri = (AgwRequestUri)uri;
      request.addHeader("rid", requestUri.getRequestId());
      String json = (new JsonEncoder()).encode(request);

      try {
         this.doAsyncInvoke(requestUri, json, callback);
      } catch (AgwRpcException var7) {
         throw var7;
      }
   }

   public abstract String doInvoke(AgwRequestUri var1, String var2) throws RequestException, AgwRpcException;

   public abstract void doAsyncInvoke(AgwRequestUri var1, String var2, ResponseCallback var3) throws RequestException, AgwRpcException;

   public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
      this.requestInterceptor = requestInterceptor;
   }
}
