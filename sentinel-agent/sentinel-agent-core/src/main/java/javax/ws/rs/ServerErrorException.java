package javax.ws.rs;

import javax.ws.rs.core.Response;

public class ServerErrorException extends WebApplicationException {
   private static final long serialVersionUID = 4730895276505569556L;

   public ServerErrorException(Response.Status status) {
      super((Throwable)null, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, Response.Status status) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(int status) {
      super((Throwable)null, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, int status) {
      super(message, (Throwable)null, (Response)validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(Response response) {
      super((Throwable)null, validate(response, Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, Response response) {
      super(message, (Throwable)null, (Response)validate(response, Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(Response.Status status, Throwable cause) {
      super(cause, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, Response.Status status, Throwable cause) {
      super(message, cause, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(int status, Throwable cause) {
      super(cause, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, int status, Throwable cause) {
      super(message, cause, validate(Response.status(status).build(), Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(Response response, Throwable cause) {
      super(cause, validate(response, Response.Status.Family.SERVER_ERROR));
   }

   public ServerErrorException(String message, Response response, Throwable cause) {
      super(message, cause, validate(response, Response.Status.Family.SERVER_ERROR));
   }
}
