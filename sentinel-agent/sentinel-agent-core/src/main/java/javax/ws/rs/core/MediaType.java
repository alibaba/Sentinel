package javax.ws.rs.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.ws.rs.ext.RuntimeDelegate;

public class MediaType {
   private String type;
   private String subtype;
   private Map<String, String> parameters;
   public static final String CHARSET_PARAMETER = "charset";
   public static final String MEDIA_TYPE_WILDCARD = "*";
   public static final String WILDCARD = "*/*";
   public static final MediaType WILDCARD_TYPE = new MediaType();
   public static final String APPLICATION_XML = "application/xml";
   public static final MediaType APPLICATION_XML_TYPE = new MediaType("application", "xml");
   public static final String APPLICATION_ATOM_XML = "application/atom+xml";
   public static final MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application", "atom+xml");
   public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
   public static final MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application", "xhtml+xml");
   public static final String APPLICATION_SVG_XML = "application/svg+xml";
   public static final MediaType APPLICATION_SVG_XML_TYPE = new MediaType("application", "svg+xml");
   public static final String APPLICATION_JSON = "application/json";
   public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application", "json");
   public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
   public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application", "x-www-form-urlencoded");
   public static final String MULTIPART_FORM_DATA = "multipart/form-data";
   public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart", "form-data");
   public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
   public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application", "octet-stream");
   public static final String TEXT_PLAIN = "text/plain";
   public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text", "plain");
   public static final String TEXT_XML = "text/xml";
   public static final MediaType TEXT_XML_TYPE = new MediaType("text", "xml");
   public static final String TEXT_HTML = "text/html";
   public static final MediaType TEXT_HTML_TYPE = new MediaType("text", "html");
   public static final String SERVER_SENT_EVENTS = "text/event-stream";
   public static final MediaType SERVER_SENT_EVENTS_TYPE = new MediaType("text", "event-stream");
   public static final String APPLICATION_JSON_PATCH_JSON = "application/json-patch+json";
   public static final MediaType APPLICATION_JSON_PATCH_JSON_TYPE = new MediaType("application", "json-patch+json");

   public static MediaType valueOf(String type) {
      return (MediaType)RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).fromString(type);
   }

   private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
      TreeMap<String, String> map = new TreeMap(new Comparator<String>() {
         public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
         }
      });
      if (initialValues != null) {
         Iterator var2 = initialValues.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<String, String> e = (Entry)var2.next();
            map.put(((String)e.getKey()).toLowerCase(), (String)e.getValue());
         }
      }

      return map;
   }

   public MediaType(String type, String subtype, Map<String, String> parameters) {
      this(type, subtype, (String)null, createParametersMap(parameters));
   }

   public MediaType(String type, String subtype) {
      this(type, subtype, (String)null, (Map)null);
   }

   public MediaType(String type, String subtype, String charset) {
      this(type, subtype, charset, (Map)null);
   }

   public MediaType() {
      this("*", "*", (String)null, (Map)null);
   }

   private MediaType(String type, String subtype, String charset, Map<String, String> parameterMap) {
      this.type = type == null ? "*" : type;
      this.subtype = subtype == null ? "*" : subtype;
      if (parameterMap == null) {
         parameterMap = new TreeMap(new Comparator<String>() {
            public int compare(String o1, String o2) {
               return o1.compareToIgnoreCase(o2);
            }
         });
      }

      if (charset != null && !charset.isEmpty()) {
         ((Map)parameterMap).put("charset", charset);
      }

      this.parameters = Collections.unmodifiableMap((Map)parameterMap);
   }

   public String getType() {
      return this.type;
   }

   public boolean isWildcardType() {
      return this.getType().equals("*");
   }

   public String getSubtype() {
      return this.subtype;
   }

   public boolean isWildcardSubtype() {
      return this.getSubtype().equals("*");
   }

   public Map<String, String> getParameters() {
      return this.parameters;
   }

   public MediaType withCharset(String charset) {
      return new MediaType(this.type, this.subtype, charset, createParametersMap(this.parameters));
   }

   public boolean isCompatible(MediaType other) {
      return other != null && (this.type.equals("*") || other.type.equals("*") || this.type.equalsIgnoreCase(other.type) && (this.subtype.equals("*") || other.subtype.equals("*")) || this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype));
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof MediaType)) {
         return false;
      } else {
         MediaType other = (MediaType)obj;
         return this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype) && this.parameters.equals(other.parameters);
      }
   }

   public int hashCode() {
      return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
   }

   public String toString() {
      return RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).toString(this);
   }
}
