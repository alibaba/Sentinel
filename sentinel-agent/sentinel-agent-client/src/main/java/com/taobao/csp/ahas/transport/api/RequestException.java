package com.taobao.csp.ahas.transport.api;

public class RequestException extends Exception {
   public RequestException() {
   }

   public RequestException(String message) {
      super(message);
   }

   public RequestException(String message, Throwable cause) {
      super(message, cause);
   }

   public RequestException(Throwable e) {
      super(e);
   }
}
