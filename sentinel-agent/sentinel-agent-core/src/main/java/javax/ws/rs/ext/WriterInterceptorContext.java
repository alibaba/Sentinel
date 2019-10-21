package javax.ws.rs.ext;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

public interface WriterInterceptorContext extends InterceptorContext {
   void proceed() throws IOException, WebApplicationException;

   Object getEntity();

   void setEntity(Object var1);

   OutputStream getOutputStream();

   void setOutputStream(OutputStream var1);

   MultivaluedMap<String, Object> getHeaders();
}
