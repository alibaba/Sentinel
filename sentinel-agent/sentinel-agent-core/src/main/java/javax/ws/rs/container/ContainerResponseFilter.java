package javax.ws.rs.container;

import java.io.IOException;

public interface ContainerResponseFilter {
   void filter(ContainerRequestContext var1, ContainerResponseContext var2) throws IOException;
}
