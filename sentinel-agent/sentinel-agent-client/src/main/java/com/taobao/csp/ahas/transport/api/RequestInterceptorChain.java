package com.taobao.csp.ahas.transport.api;

public abstract class RequestInterceptorChain implements RequestInterceptor {
   private RequestInterceptor interceptor;

   public RequestInterceptor setInterceptor(RequestInterceptor interceptor) {
      this.interceptor = interceptor;
      return interceptor;
   }

   public RequestInterceptor getInterceptor() {
      return this.interceptor;
   }

   public void handle(Request request) throws RequestException {
      this.doHandle(request);
      if (this.interceptor != null) {
         this.interceptor.handle(request);
      }

   }

   public void invoke(Request request) throws RequestException {
      this.doInvoke(request);
      if (this.interceptor != null) {
         this.interceptor.invoke(request);
      }

   }

   public abstract void doHandle(Request var1) throws RequestException;

   public abstract void doInvoke(Request var1) throws RequestException;
}
