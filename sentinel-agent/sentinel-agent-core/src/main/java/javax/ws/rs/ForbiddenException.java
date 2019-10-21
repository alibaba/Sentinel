package javax.ws.rs;

import javax.ws.rs.core.Response;

public class ForbiddenException extends ClientErrorException {
   private static final long serialVersionUID = -2740045367479165061L;

   public ForbiddenException() {
      super(Response.Status.FORBIDDEN);
   }

   public ForbiddenException(String message) {
      super(message, Response.Status.FORBIDDEN);
   }

   public ForbiddenException(Response response) {
      super(validate(response, Response.Status.FORBIDDEN));
   }

   public ForbiddenException(String message, Response response) {
      super(message, validate(response, Response.Status.FORBIDDEN));
   }

   public ForbiddenException(Throwable cause) {
      super(Response.Status.FORBIDDEN, cause);
   }

   public ForbiddenException(String message, Throwable cause) {
      super(message, Response.Status.FORBIDDEN, cause);
   }

   public ForbiddenException(Response response, Throwable cause) {
      super(validate(response, Response.Status.FORBIDDEN), cause);
   }

   public ForbiddenException(String message, Response response, Throwable cause) {
      super(message, validate(response, Response.Status.FORBIDDEN), cause);
   }
}
