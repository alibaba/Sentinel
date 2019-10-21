package javax.ws.rs.client;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

public interface Client extends Configurable<Client> {
   void close();

   WebTarget target(String var1);

   WebTarget target(URI var1);

   WebTarget target(UriBuilder var1);

   WebTarget target(Link var1);

   Invocation.Builder invocation(Link var1);

   SSLContext getSslContext();

   HostnameVerifier getHostnameVerifier();
}
