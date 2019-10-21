package javax.ws.rs.core;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.ext.RuntimeDelegate;

public abstract class UriBuilder {
   protected UriBuilder() {
   }

   protected static UriBuilder newInstance() {
      return RuntimeDelegate.getInstance().createUriBuilder();
   }

   public static UriBuilder fromUri(URI uri) {
      return newInstance().uri(uri);
   }

   public static UriBuilder fromUri(String uriTemplate) {
      return newInstance().uri(uriTemplate);
   }

   public static UriBuilder fromLink(Link link) {
      if (link == null) {
         throw new IllegalArgumentException("The provider 'link' parameter value is 'null'.");
      } else {
         return fromUri(link.getUri());
      }
   }

   public static UriBuilder fromPath(String path) throws IllegalArgumentException {
      return newInstance().path(path);
   }

   public static UriBuilder fromResource(Class<?> resource) {
      return newInstance().path(resource);
   }

   public static UriBuilder fromMethod(Class<?> resource, String method) {
      return newInstance().path(resource, method);
   }

   public abstract UriBuilder clone();

   public abstract UriBuilder uri(URI var1);

   public abstract UriBuilder uri(String var1);

   public abstract UriBuilder scheme(String var1);

   public abstract UriBuilder schemeSpecificPart(String var1);

   public abstract UriBuilder userInfo(String var1);

   public abstract UriBuilder host(String var1);

   public abstract UriBuilder port(int var1);

   public abstract UriBuilder replacePath(String var1);

   public abstract UriBuilder path(String var1);

   public abstract UriBuilder path(Class var1);

   public abstract UriBuilder path(Class var1, String var2);

   public abstract UriBuilder path(Method var1);

   public abstract UriBuilder segment(String... var1);

   public abstract UriBuilder replaceMatrix(String var1);

   public abstract UriBuilder matrixParam(String var1, Object... var2);

   public abstract UriBuilder replaceMatrixParam(String var1, Object... var2);

   public abstract UriBuilder replaceQuery(String var1);

   public abstract UriBuilder queryParam(String var1, Object... var2);

   public abstract UriBuilder replaceQueryParam(String var1, Object... var2);

   public abstract UriBuilder fragment(String var1);

   public abstract UriBuilder resolveTemplate(String var1, Object var2);

   public abstract UriBuilder resolveTemplate(String var1, Object var2, boolean var3);

   public abstract UriBuilder resolveTemplateFromEncoded(String var1, Object var2);

   public abstract UriBuilder resolveTemplates(Map<String, Object> var1);

   public abstract UriBuilder resolveTemplates(Map<String, Object> var1, boolean var2) throws IllegalArgumentException;

   public abstract UriBuilder resolveTemplatesFromEncoded(Map<String, Object> var1);

   public abstract URI buildFromMap(Map<String, ?> var1);

   public abstract URI buildFromMap(Map<String, ?> var1, boolean var2) throws IllegalArgumentException, UriBuilderException;

   public abstract URI buildFromEncodedMap(Map<String, ?> var1) throws IllegalArgumentException, UriBuilderException;

   public abstract URI build(Object... var1) throws IllegalArgumentException, UriBuilderException;

   public abstract URI build(Object[] var1, boolean var2) throws IllegalArgumentException, UriBuilderException;

   public abstract URI buildFromEncoded(Object... var1) throws IllegalArgumentException, UriBuilderException;

   public abstract String toTemplate();
}
