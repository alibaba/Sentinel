package javax.ws.rs;

import javax.ws.rs.core.Response;

public class BadRequestException extends ClientErrorException {
   private static final long serialVersionUID = 7264647684649480265L;

   public BadRequestException() {
      super(Response.Status.BAD_REQUEST);
   }

   public BadRequestException(String message) {
      super(message, Response.Status.BAD_REQUEST);
   }

   public BadRequestException(Response response) {
      super(validate(response, Response.Status.BAD_REQUEST));
   }

   public BadRequestException(String message, Response response) {
      super(message, validate(response, Response.Status.BAD_REQUEST));
   }

   public BadRequestException(Throwable cause) {
      super(Response.Status.BAD_REQUEST, cause);
   }

   public BadRequestException(String message, Throwable cause) {
      super(message, Response.Status.BAD_REQUEST, cause);
   }

   public BadRequestException(Response response, Throwable cause) {
      super(validate(response, Response.Status.BAD_REQUEST), cause);
   }

   public BadRequestException(String message, Response response, Throwable cause) {
      super(message, validate(response, Response.Status.BAD_REQUEST), cause);
   }
}
