package javax.ws.rs.client;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

public class ResponseProcessingException extends ProcessingException {
   private static final long serialVersionUID = -4923161617935731839L;
   private final Response response;

   public ResponseProcessingException(Response response, Throwable cause) {
      super(cause);
      this.response = response;
   }

   public ResponseProcessingException(Response response, String message, Throwable cause) {
      super(message, cause);
      this.response = response;
   }

   public ResponseProcessingException(Response response, String message) {
      super(message);
      this.response = response;
   }

   public Response getResponse() {
      return this.response;
   }
}
