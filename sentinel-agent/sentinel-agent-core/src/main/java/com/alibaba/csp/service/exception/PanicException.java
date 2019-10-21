package com.alibaba.csp.service.exception;

public class PanicException extends AhasException {
   public PanicException() {
   }

   public PanicException(String message) {
      super(message);
   }

   public PanicException(String message, Throwable cause) {
      super(message, cause);
   }

   public PanicException(Throwable cause) {
      super(cause);
   }
}
