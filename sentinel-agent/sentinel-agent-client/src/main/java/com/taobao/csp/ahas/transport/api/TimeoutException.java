package com.taobao.csp.ahas.transport.api;

public class TimeoutException extends RequestException {
   public TimeoutException() {
   }

   public TimeoutException(String message) {
      super(message);
   }

   public TimeoutException(String message, Throwable cause) {
      super(message, cause);
   }

   public TimeoutException(Throwable e) {
      super(e);
   }
}
