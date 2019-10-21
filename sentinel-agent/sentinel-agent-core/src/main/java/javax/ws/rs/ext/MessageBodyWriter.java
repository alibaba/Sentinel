package javax.ws.rs.ext;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public interface MessageBodyWriter<T> {
   boolean isWriteable(Class<?> var1, Type var2, Annotation[] var3, MediaType var4);

   default long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1L;
   }

   void writeTo(T var1, Class<?> var2, Type var3, Annotation[] var4, MediaType var5, MultivaluedMap<String, Object> var6, OutputStream var7) throws IOException, WebApplicationException;
}
