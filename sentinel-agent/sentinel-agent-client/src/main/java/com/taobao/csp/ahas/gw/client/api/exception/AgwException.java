package com.taobao.csp.ahas.gw.client.api.exception;

public class AgwException extends RuntimeException {
   public AgwException(String errorMsg, Throwable t) {
      super(errorMsg, t);
   }
}
