package javax.ws.rs.ext;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

public interface ReaderInterceptorContext extends InterceptorContext {
   Object proceed() throws IOException, WebApplicationException;

   InputStream getInputStream();

   void setInputStream(InputStream var1);

   MultivaluedMap<String, String> getHeaders();
}
