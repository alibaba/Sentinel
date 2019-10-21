package com.alibaba.csp.service.exception;

public class AhasException extends Exception {
   public AhasException() {
   }

   public AhasException(String message) {
      super(message);
   }

   public AhasException(String message, Throwable cause) {
      super(message, cause);
   }

   public AhasException(Throwable cause) {
      super(cause);
   }
}
