package javax.ws.rs.core;

import java.io.IOException;

public class NoContentException extends IOException {
   private static final long serialVersionUID = -3082577759787473245L;

   public NoContentException(String message) {
      super(message);
   }

   public NoContentException(String message, Throwable cause) {
      super(message, cause);
   }

   public NoContentException(Throwable cause) {
      super(cause);
   }
}
