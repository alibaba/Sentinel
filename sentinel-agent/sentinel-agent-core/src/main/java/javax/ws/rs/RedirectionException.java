package javax.ws.rs;

import java.net.URI;
import javax.ws.rs.core.Response;

public class RedirectionException extends WebApplicationException {
   private static final long serialVersionUID = -2584325408291098012L;

   public RedirectionException(Response.Status status, URI location) {
      super((Throwable)null, validate(Response.status(status).location(location).build(), Response.Status.Family.REDIRECTION));
   }

   public RedirectionException(String message, Response.Status status, URI location) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).location(location).build(), Response.Status.Family.REDIRECTION));
   }

   public RedirectionException(int status, URI location) {
      super((Throwable)null, validate(Response.status(status).location(location).build(), Response.Status.Family.REDIRECTION));
   }

   public RedirectionException(String message, int status, URI location) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).location(location).build(), Response.Status.Family.REDIRECTION));
   }

   public RedirectionException(Response response) {
      super((Throwable)null, validate(response, Response.Status.Family.REDIRECTION));
   }

   public RedirectionException(String message, Response response) {
      super(message, (Throwable)null, (Response)validate(response, Response.Status.Family.REDIRECTION));
   }

   public URI getLocation() {
      return this.getResponse().getLocation();
   }
}
