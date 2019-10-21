package javax.ws.rs.core;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface HttpHeaders {
   String ACCEPT = "Accept";
   String ACCEPT_CHARSET = "Accept-Charset";
   String ACCEPT_ENCODING = "Accept-Encoding";
   String ACCEPT_LANGUAGE = "Accept-Language";
   String ALLOW = "Allow";
   String AUTHORIZATION = "Authorization";
   String CACHE_CONTROL = "Cache-Control";
   String CONTENT_DISPOSITION = "Content-Disposition";
   String CONTENT_ENCODING = "Content-Encoding";
   String CONTENT_ID = "Content-ID";
   String CONTENT_LANGUAGE = "Content-Language";
   String CONTENT_LENGTH = "Content-Length";
   String CONTENT_LOCATION = "Content-Location";
   String CONTENT_TYPE = "Content-Type";
   String DATE = "Date";
   String ETAG = "ETag";
   String EXPIRES = "Expires";
   String HOST = "Host";
   String IF_MATCH = "If-Match";
   String IF_MODIFIED_SINCE = "If-Modified-Since";
   String IF_NONE_MATCH = "If-None-Match";
   String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
   String LAST_MODIFIED = "Last-Modified";
   String LOCATION = "Location";
   String LINK = "Link";
   String RETRY_AFTER = "Retry-After";
   String USER_AGENT = "User-Agent";
   String VARY = "Vary";
   String WWW_AUTHENTICATE = "WWW-Authenticate";
   String COOKIE = "Cookie";
   String SET_COOKIE = "Set-Cookie";
   String LAST_EVENT_ID_HEADER = "Last-Event-ID";

   List<String> getRequestHeader(String var1);

   String getHeaderString(String var1);

   MultivaluedMap<String, String> getRequestHeaders();

   List<MediaType> getAcceptableMediaTypes();

   List<Locale> getAcceptableLanguages();

   MediaType getMediaType();

   Locale getLanguage();

   Map<String, Cookie> getCookies();

   Date getDate();

   int getLength();
}
