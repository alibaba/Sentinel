package javax.ws.rs.client;

import java.io.IOException;

public interface ClientResponseFilter {
   void filter(ClientRequestContext var1, ClientResponseContext var2) throws IOException;
}
