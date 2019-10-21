package javax.ws.rs.container;

import java.io.IOException;

public interface ContainerRequestFilter {
   void filter(ContainerRequestContext var1) throws IOException;
}
