package com.google.inject;

public final class OutOfScopeException extends RuntimeException {
   public OutOfScopeException(String message) {
      super(message);
   }

   public OutOfScopeException(String message, Throwable cause) {
      super(message, cause);
   }

   public OutOfScopeException(Throwable cause) {
      super(cause);
   }
}
