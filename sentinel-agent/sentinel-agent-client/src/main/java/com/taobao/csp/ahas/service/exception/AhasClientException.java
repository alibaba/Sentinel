package com.taobao.csp.ahas.service.exception;

public class AhasClientException extends Exception {
   public AhasClientException() {
   }

   public AhasClientException(String message) {
      super(message);
   }

   public AhasClientException(String message, Throwable cause) {
      super(message, cause);
   }

   public AhasClientException(Throwable cause) {
      super(cause);
   }
}
