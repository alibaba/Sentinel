package javax.ws.rs.core;

import javax.ws.rs.ext.RuntimeDelegate;

public class Cookie {
   public static final int DEFAULT_VERSION = 1;
   private static final RuntimeDelegate.HeaderDelegate<Cookie> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(Cookie.class);
   private final String name;
   private final String value;
   private final int version;
   private final String path;
   private final String domain;

   public Cookie(String name, String value, String path, String domain, int version) throws IllegalArgumentException {
      if (name == null) {
         throw new IllegalArgumentException("name==null");
      } else {
         this.name = name;
         this.value = value;
         this.version = version;
         this.domain = domain;
         this.path = path;
      }
   }

   public Cookie(String name, String value, String path, String domain) throws IllegalArgumentException {
      this(name, value, path, domain, 1);
   }

   public Cookie(String name, String value) throws IllegalArgumentException {
      this(name, value, (String)null, (String)null);
   }

   public static Cookie valueOf(String value) {
      return (Cookie)HEADER_DELEGATE.fromString(value);
   }

   public String getName() {
      return this.name;
   }

   public String getValue() {
      return this.value;
   }

   public int getVersion() {
      return this.version;
   }

   public String getDomain() {
      return this.domain;
   }

   public String getPath() {
      return this.path;
   }

   public String toString() {
      return HEADER_DELEGATE.toString(this);
   }

   public int hashCode() {
      int hash = 7;
      hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
      hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
      hash = 97 * hash + this.version;
      hash = 97 * hash + (this.path != null ? this.path.hashCode() : 0);
      hash = 97 * hash + (this.domain != null ? this.domain.hashCode() : 0);
      return hash;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Cookie other = (Cookie)obj;
         if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
         } else if (this.value == other.value || this.value != null && this.value.equals(other.value)) {
            if (this.version != other.version) {
               return false;
            } else if (this.path == other.path || this.path != null && this.path.equals(other.path)) {
               return this.domain == other.domain || this.domain != null && this.domain.equals(other.domain);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }
}
