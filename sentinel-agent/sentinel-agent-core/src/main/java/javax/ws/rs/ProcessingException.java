package javax.ws.rs;

public class ProcessingException extends RuntimeException {
   private static final long serialVersionUID = -4232431597816056514L;

   public ProcessingException(Throwable cause) {
      super(cause);
   }

   public ProcessingException(String message, Throwable cause) {
      super(message, cause);
   }

   public ProcessingException(String message) {
      super(message);
   }
}
