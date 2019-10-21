package javax.ws.rs.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.ws.rs.core.MediaType;

public interface InterceptorContext {
   Object getProperty(String var1);

   Collection<String> getPropertyNames();

   void setProperty(String var1, Object var2);

   void removeProperty(String var1);

   Annotation[] getAnnotations();

   void setAnnotations(Annotation[] var1);

   Class<?> getType();

   void setType(Class<?> var1);

   Type getGenericType();

   void setGenericType(Type var1);

   MediaType getMediaType();

   void setMediaType(MediaType var1);
}
