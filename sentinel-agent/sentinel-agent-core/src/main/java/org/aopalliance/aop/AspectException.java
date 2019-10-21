package org.aopalliance.aop;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AspectException extends RuntimeException {
   private String message;
   private String stackTrace;
   private Throwable t;

   public Throwable getCause() {
      return this.t;
   }

   public String toString() {
      return this.getMessage();
   }

   public String getMessage() {
      return this.message;
   }

   public void printStackTrace() {
      System.err.print(this.stackTrace);
   }

   public void printStackTrace(PrintStream var1) {
      this.printStackTrace(new PrintWriter(var1));
   }

   public void printStackTrace(PrintWriter var1) {
      var1.print(this.stackTrace);
   }

   public AspectException(String var1) {
      super(var1);
      this.message = var1;
      this.stackTrace = var1;
   }

   public AspectException(String var1, Throwable var2) {
      super(var1 + "; nested exception is " + var2.getMessage());
      this.t = var2;
      StringWriter var3 = new StringWriter();
      var2.printStackTrace(new PrintWriter(var3));
      this.stackTrace = var3.toString();
   }
}
