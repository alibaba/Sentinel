package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.ahas.auth.api.AuthException;
import com.taobao.csp.ahas.gw.client.api.handler.AgwHandler;

public abstract class AgwRequestHandler<R> implements RequestHandler, AgwHandler {
   private RequestInterceptor requestInterceptor;

   public String handle(String json) {
      Request request = null;
      Response response = null;

      try {
         request = (new RequestJsonDecoder()).decode(json);
         if (this.requestInterceptor != null) {
            this.requestInterceptor.handle(request);
         }

         response = this.handle(request);
      } catch (InvalidTimestampException var6) {
         this.logHandleException(request, var6);
         response = Response.ofFailure(Response.Code.INVALID_TIMESTAMP, var6.getMessage());
      } catch (AuthException var7) {
         this.logHandleException(request, var7);
         response = Response.ofFailure(Response.Code.FORBIDDEN, var7.getMessage());
      } catch (Exception var8) {
         this.logHandleException(request, var8);
         response = Response.ofFailure(Response.Code.SERVER_ERROR, var8.getMessage());
      }

      try {
         String resp = (new JsonEncoder()).encode(response);
         return resp;
      } catch (EncoderException var5) {
         this.logHandleException(request, var5);
         throw new RuntimeException(var5);
      }
   }

   public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
      this.requestInterceptor = requestInterceptor;
   }

   public abstract void logHandleException(Request var1, Exception var2);
}
