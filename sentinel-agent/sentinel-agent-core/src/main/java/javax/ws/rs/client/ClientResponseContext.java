package javax.ws.rs.client;

import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface ClientResponseContext {
   int getStatus();

   void setStatus(int var1);

   Response.StatusType getStatusInfo();

   void setStatusInfo(Response.StatusType var1);

   MultivaluedMap<String, String> getHeaders();

   String getHeaderString(String var1);

   Set<String> getAllowedMethods();

   Date getDate();

   Locale getLanguage();

   int getLength();

   MediaType getMediaType();

   Map<String, NewCookie> getCookies();

   EntityTag getEntityTag();

   Date getLastModified();

   URI getLocation();

   Set<Link> getLinks();

   boolean hasLink(String var1);

   Link getLink(String var1);

   Link.Builder getLinkBuilder(String var1);

   boolean hasEntity();

   InputStream getEntityStream();

   void setEntityStream(InputStream var1);
}
