package com.taobao.csp.ahas.transport.api;

public class EncoderException extends RequestException {
   public EncoderException() {
   }

   public EncoderException(String message) {
      super(message);
   }

   public EncoderException(String message, Throwable cause) {
      super(message, cause);
   }

   public EncoderException(Throwable cause) {
      super(cause);
   }
}
