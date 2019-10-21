package javax.ws.rs.client;

import java.io.IOException;

public interface ClientRequestFilter {
   void filter(ClientRequestContext var1) throws IOException;
}
