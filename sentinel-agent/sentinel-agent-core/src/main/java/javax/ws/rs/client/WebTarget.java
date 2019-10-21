package javax.ws.rs.client;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public interface WebTarget extends Configurable<WebTarget> {
   URI getUri();

   UriBuilder getUriBuilder();

   WebTarget path(String var1);

   WebTarget resolveTemplate(String var1, Object var2);

   WebTarget resolveTemplate(String var1, Object var2, boolean var3);

   WebTarget resolveTemplateFromEncoded(String var1, Object var2);

   WebTarget resolveTemplates(Map<String, Object> var1);

   WebTarget resolveTemplates(Map<String, Object> var1, boolean var2);

   WebTarget resolveTemplatesFromEncoded(Map<String, Object> var1);

   WebTarget matrixParam(String var1, Object... var2);

   WebTarget queryParam(String var1, Object... var2);

   Invocation.Builder request();

   Invocation.Builder request(String... var1);

   Invocation.Builder request(MediaType... var1);
}
