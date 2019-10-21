package javax.ws.rs.ext;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;

public interface ReaderInterceptor {
   Object aroundReadFrom(ReaderInterceptorContext var1) throws IOException, WebApplicationException;
}
