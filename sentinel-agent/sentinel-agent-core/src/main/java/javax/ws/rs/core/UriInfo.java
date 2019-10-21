package javax.ws.rs.core;

import java.net.URI;
import java.util.List;

public interface UriInfo {
   String getPath();

   String getPath(boolean var1);

   List<PathSegment> getPathSegments();

   List<PathSegment> getPathSegments(boolean var1);

   URI getRequestUri();

   UriBuilder getRequestUriBuilder();

   URI getAbsolutePath();

   UriBuilder getAbsolutePathBuilder();

   URI getBaseUri();

   UriBuilder getBaseUriBuilder();

   MultivaluedMap<String, String> getPathParameters();

   MultivaluedMap<String, String> getPathParameters(boolean var1);

   MultivaluedMap<String, String> getQueryParameters();

   MultivaluedMap<String, String> getQueryParameters(boolean var1);

   List<String> getMatchedURIs();

   List<String> getMatchedURIs(boolean var1);

   List<Object> getMatchedResources();

   URI resolve(URI var1);

   URI relativize(URI var1);
}
