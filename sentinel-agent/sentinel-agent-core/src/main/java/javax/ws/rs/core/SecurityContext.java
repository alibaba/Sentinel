package javax.ws.rs.core;

import java.security.Principal;

public interface SecurityContext {
   String BASIC_AUTH = "BASIC";
   String CLIENT_CERT_AUTH = "CLIENT_CERT";
   String DIGEST_AUTH = "DIGEST";
   String FORM_AUTH = "FORM";

   Principal getUserPrincipal();

   boolean isUserInRole(String var1);

   boolean isSecure();

   String getAuthenticationScheme();
}
