package javax.ws.rs;

import java.util.Date;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

public class ServiceUnavailableException extends ServerErrorException {
   private static final long serialVersionUID = 3821068205617492633L;

   public ServiceUnavailableException() {
      super(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
   }

   public ServiceUnavailableException(String message) {
      super(message, Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
   }

   public ServiceUnavailableException(Long retryAfter) {
      super(Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build());
   }

   public ServiceUnavailableException(String message, Long retryAfter) {
      super(message, Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build());
   }

   public ServiceUnavailableException(Date retryAfter) {
      super(Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build());
   }

   public ServiceUnavailableException(String message, Date retryAfter) {
      super(message, Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build());
   }

   public ServiceUnavailableException(Response response) {
      super(validate(response, Response.Status.SERVICE_UNAVAILABLE));
   }

   public ServiceUnavailableException(String message, Response response) {
      super(message, validate(response, Response.Status.SERVICE_UNAVAILABLE));
   }

   public ServiceUnavailableException(Date retryAfter, Throwable cause) {
      super(Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build(), cause);
   }

   public ServiceUnavailableException(String message, Date retryAfter, Throwable cause) {
      super(message, Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build(), cause);
   }

   public ServiceUnavailableException(Long retryAfter, Throwable cause) {
      super(Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build(), cause);
   }

   public ServiceUnavailableException(String message, Long retryAfter, Throwable cause) {
      super(message, Response.status(Response.Status.SERVICE_UNAVAILABLE).header("Retry-After", retryAfter).build(), cause);
   }

   public ServiceUnavailableException(Response response, Throwable cause) {
      super(validate(response, Response.Status.SERVICE_UNAVAILABLE), cause);
   }

   public ServiceUnavailableException(String message, Response response, Throwable cause) {
      super(message, validate(response, Response.Status.SERVICE_UNAVAILABLE), cause);
   }

   public boolean hasRetryAfter() {
      return this.getResponse().getHeaders().containsKey("Retry-After");
   }

   public Date getRetryTime(Date requestTime) {
      String value = this.getResponse().getHeaderString("Retry-After");
      if (value == null) {
         return null;
      } else {
         try {
            Long interval = Long.parseLong(value);
            return new Date(requestTime.getTime() + interval * 1000L);
         } catch (NumberFormatException var4) {
            RuntimeDelegate.HeaderDelegate<Date> dateDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);
            return (Date)dateDelegate.fromString(value);
         }
      }
   }
}
