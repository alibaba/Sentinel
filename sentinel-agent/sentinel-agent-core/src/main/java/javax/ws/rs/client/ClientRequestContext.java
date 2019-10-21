package javax.ws.rs.client;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public interface ClientRequestContext {
   Object getProperty(String var1);

   Collection<String> getPropertyNames();

   void setProperty(String var1, Object var2);

   void removeProperty(String var1);

   URI getUri();

   void setUri(URI var1);

   String getMethod();

   void setMethod(String var1);

   MultivaluedMap<String, Object> getHeaders();

   MultivaluedMap<String, String> getStringHeaders();

   String getHeaderString(String var1);

   Date getDate();

   Locale getLanguage();

   MediaType getMediaType();

   List<MediaType> getAcceptableMediaTypes();

   List<Locale> getAcceptableLanguages();

   Map<String, Cookie> getCookies();

   boolean hasEntity();

   Object getEntity();

   Class<?> getEntityClass();

   Type getEntityType();

   void setEntity(Object var1);

   void setEntity(Object var1, Annotation[] var2, MediaType var3);

   Annotation[] getEntityAnnotations();

   OutputStream getEntityStream();

   void setEntityStream(OutputStream var1);

   Client getClient();

   Configuration getConfiguration();

   void abortWith(Response var1);
}
