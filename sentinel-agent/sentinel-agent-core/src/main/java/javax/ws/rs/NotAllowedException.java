package javax.ws.rs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Response;

public class NotAllowedException extends ClientErrorException {
   private static final long serialVersionUID = -586776054369626119L;

   public NotAllowedException(String allowed, String... moreAllowed) {
      super(validateAllow(createNotAllowedResponse(allowed, moreAllowed)));
   }

   public NotAllowedException(String message, String allowed, String... moreAllowed) {
      super(message, validateAllow(createNotAllowedResponse(allowed, moreAllowed)));
   }

   private static Response createNotAllowedResponse(String allowed, String... moreAllowed) {
      if (allowed == null) {
         throw new NullPointerException("No allowed method specified.");
      } else {
         Object methods;
         if (moreAllowed != null && moreAllowed.length > 0) {
            methods = new HashSet(moreAllowed.length + 1);
            ((Set)methods).add(allowed);
            Collections.addAll((Collection)methods, moreAllowed);
         } else {
            methods = Collections.singleton(allowed);
         }

         return Response.status(Response.Status.METHOD_NOT_ALLOWED).allow((Set)methods).build();
      }
   }

   public NotAllowedException(Response response) {
      super(validate(response, Response.Status.METHOD_NOT_ALLOWED));
   }

   public NotAllowedException(String message, Response response) {
      super(message, validate(response, Response.Status.METHOD_NOT_ALLOWED));
   }

   public NotAllowedException(Throwable cause, String... allowedMethods) {
      super(validateAllow(Response.status(Response.Status.METHOD_NOT_ALLOWED).allow(allowedMethods).build()), cause);
   }

   public NotAllowedException(String message, Throwable cause, String... allowedMethods) {
      super(message, validateAllow(Response.status(Response.Status.METHOD_NOT_ALLOWED).allow(allowedMethods).build()), cause);
   }

   public NotAllowedException(Response response, Throwable cause) {
      super(validateAllow(validate(response, Response.Status.METHOD_NOT_ALLOWED)), cause);
   }

   public NotAllowedException(String message, Response response, Throwable cause) {
      super(message, validateAllow(validate(response, Response.Status.METHOD_NOT_ALLOWED)), cause);
   }

   private static Response validateAllow(Response response) {
      if (!response.getHeaders().containsKey("Allow")) {
         throw new IllegalArgumentException("Response does not contain required 'Allow' HTTP header.");
      } else {
         return response;
      }
   }
}
