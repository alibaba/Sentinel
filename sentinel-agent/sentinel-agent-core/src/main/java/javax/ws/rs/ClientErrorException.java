package javax.ws.rs;

import javax.ws.rs.core.Response;

public class ClientErrorException extends WebApplicationException {
   private static final long serialVersionUID = -4101970664444907990L;

   public ClientErrorException(Response.Status status) {
      super((Throwable)null, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, Response.Status status) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(int status) {
      super((Throwable)null, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, int status) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(Response response) {
      super((Throwable)null, validate(response, Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, Response response) {
      super(message, (Throwable)null, (Response)validate(response, Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(Response.Status status, Throwable cause) {
      super(cause, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, Response.Status status, Throwable cause) {
      super(message, cause, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(int status, Throwable cause) {
      super(cause, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, int status, Throwable cause) {
      super(message, cause, validate(Response.status(status).build(), Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(Response response, Throwable cause) {
      super(cause, validate(response, Response.Status.Family.CLIENT_ERROR));
   }

   public ClientErrorException(String message, Response response, Throwable cause) {
      super(message, cause, validate(response, Response.Status.Family.CLIENT_ERROR));
   }
}
