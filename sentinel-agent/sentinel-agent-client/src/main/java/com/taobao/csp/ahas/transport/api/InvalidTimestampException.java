package com.taobao.csp.ahas.transport.api;

public class InvalidTimestampException extends RequestException {
   public InvalidTimestampException() {
   }

   public InvalidTimestampException(String message) {
      super(message);
   }

   public InvalidTimestampException(String message, Throwable cause) {
      super(message, cause);
   }

   public InvalidTimestampException(Throwable e) {
      super(e);
   }
}
