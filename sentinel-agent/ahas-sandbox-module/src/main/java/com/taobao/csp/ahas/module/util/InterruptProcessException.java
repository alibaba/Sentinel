package com.taobao.csp.ahas.module.util;

public class InterruptProcessException extends Exception {
   private final InterruptProcessException.State state;
   private final Object response;

   private InterruptProcessException(InterruptProcessException.State state, Object response) {
      this.state = state;
      this.response = response;
   }

   public static InterruptProcessException throwReturnImmediately(Object object) throws InterruptProcessException {
      throw new InterruptProcessException(InterruptProcessException.State.RETURN_IMMEDIATELY, object);
   }

   public static InterruptProcessException throwThrowsImmediately(Throwable throwable) throws InterruptProcessException {
      throw new InterruptProcessException(InterruptProcessException.State.THROWS_IMMEDIATELY, throwable);
   }

   public InterruptProcessException.State getState() {
      return this.state;
   }

   public Object getResponse() {
      return this.response;
   }

   public static enum State {
      RETURN_IMMEDIATELY,
      THROWS_IMMEDIATELY;
   }
}
