package javax.ws.rs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

public class NotAuthorizedException extends ClientErrorException {
   private static final long serialVersionUID = -3156040750581929702L;
   private transient List<Object> challenges;

   public NotAuthorizedException(Object challenge, Object... moreChallenges) {
      super(createUnauthorizedResponse(challenge, moreChallenges));
      this.challenges = cacheChallenges(challenge, moreChallenges);
   }

   public NotAuthorizedException(String message, Object challenge, Object... moreChallenges) {
      super(message, createUnauthorizedResponse(challenge, moreChallenges));
      this.challenges = cacheChallenges(challenge, moreChallenges);
   }

   public NotAuthorizedException(Response response) {
      super(validate(response, Response.Status.UNAUTHORIZED));
   }

   public NotAuthorizedException(String message, Response response) {
      super(message, validate(response, Response.Status.UNAUTHORIZED));
   }

   public NotAuthorizedException(Throwable cause, Object challenge, Object... moreChallenges) {
      super(createUnauthorizedResponse(challenge, moreChallenges), cause);
      this.challenges = cacheChallenges(challenge, moreChallenges);
   }

   public NotAuthorizedException(String message, Throwable cause, Object challenge, Object... moreChallenges) {
      super(message, createUnauthorizedResponse(challenge, moreChallenges), cause);
      this.challenges = cacheChallenges(challenge, moreChallenges);
   }

   public NotAuthorizedException(Response response, Throwable cause) {
      super(validate(response, Response.Status.UNAUTHORIZED), cause);
   }

   public NotAuthorizedException(String message, Response response, Throwable cause) {
      super(message, validate(response, Response.Status.UNAUTHORIZED), cause);
   }

   public List<Object> getChallenges() {
      if (this.challenges == null) {
         this.challenges = (List)this.getResponse().getHeaders().get("WWW-Authenticate");
      }

      return this.challenges;
   }

   private static Response createUnauthorizedResponse(Object challenge, Object[] otherChallenges) {
      if (challenge == null) {
         throw new NullPointerException("Primary challenge parameter must not be null.");
      } else {
         Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", challenge);
         if (otherChallenges != null) {
            Object[] var3 = otherChallenges;
            int var4 = otherChallenges.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Object oc = var3[var5];
               builder.header("WWW-Authenticate", oc);
            }
         }

         return builder.build();
      }
   }

   private static List<Object> cacheChallenges(Object challenge, Object[] moreChallenges) {
      List<Object> temp = new ArrayList(1 + (moreChallenges == null ? 0 : moreChallenges.length));
      temp.add(challenge);
      if (moreChallenges != null) {
         temp.addAll(Arrays.asList(moreChallenges));
      }

      return Collections.unmodifiableList(temp);
   }
}
