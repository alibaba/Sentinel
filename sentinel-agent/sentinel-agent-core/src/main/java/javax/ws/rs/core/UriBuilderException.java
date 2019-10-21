package javax.ws.rs.core;

public class UriBuilderException extends RuntimeException {
   private static final long serialVersionUID = 956255913370721193L;

   public UriBuilderException() {
   }

   public UriBuilderException(String msg) {
      super(msg);
   }

   public UriBuilderException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public UriBuilderException(Throwable cause) {
      super(cause);
   }
}
