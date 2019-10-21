package javax.ws.rs;

import javax.ws.rs.core.Response;

public class NotSupportedException extends ClientErrorException {
   private static final long serialVersionUID = -8286622745725405656L;

   public NotSupportedException() {
      super(Response.Status.UNSUPPORTED_MEDIA_TYPE);
   }

   public NotSupportedException(String message) {
      super(message, Response.Status.UNSUPPORTED_MEDIA_TYPE);
   }

   public NotSupportedException(Response response) {
      super(validate(response, Response.Status.UNSUPPORTED_MEDIA_TYPE));
   }

   public NotSupportedException(String message, Response response) {
      super(message, validate(response, Response.Status.UNSUPPORTED_MEDIA_TYPE));
   }

   public NotSupportedException(Throwable cause) {
      super(Response.Status.UNSUPPORTED_MEDIA_TYPE, cause);
   }

   public NotSupportedException(String message, Throwable cause) {
      super(message, Response.Status.UNSUPPORTED_MEDIA_TYPE, cause);
   }

   public NotSupportedException(Response response, Throwable cause) {
      super(validate(response, Response.Status.UNSUPPORTED_MEDIA_TYPE), cause);
   }

   public NotSupportedException(String message, Response response, Throwable cause) {
      super(message, validate(response, Response.Status.UNSUPPORTED_MEDIA_TYPE), cause);
   }
}
