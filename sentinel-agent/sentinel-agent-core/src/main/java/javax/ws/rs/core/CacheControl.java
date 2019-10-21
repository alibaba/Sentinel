package javax.ws.rs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.ext.RuntimeDelegate;

public class CacheControl {
   private static final RuntimeDelegate.HeaderDelegate<CacheControl> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);
   private List<String> privateFields;
   private List<String> noCacheFields;
   private Map<String, String> cacheExtension;
   private boolean privateFlag = false;
   private boolean noCache = false;
   private boolean noStore = false;
   private boolean noTransform = true;
   private boolean mustRevalidate = false;
   private boolean proxyRevalidate = false;
   private int maxAge = -1;
   private int sMaxAge = -1;

   public static CacheControl valueOf(String value) {
      return (CacheControl)HEADER_DELEGATE.fromString(value);
   }

   public boolean isMustRevalidate() {
      return this.mustRevalidate;
   }

   public void setMustRevalidate(boolean mustRevalidate) {
      this.mustRevalidate = mustRevalidate;
   }

   public boolean isProxyRevalidate() {
      return this.proxyRevalidate;
   }

   public void setProxyRevalidate(boolean proxyRevalidate) {
      this.proxyRevalidate = proxyRevalidate;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public void setMaxAge(int maxAge) {
      this.maxAge = maxAge;
   }

   public int getSMaxAge() {
      return this.sMaxAge;
   }

   public void setSMaxAge(int sMaxAge) {
      this.sMaxAge = sMaxAge;
   }

   public List<String> getNoCacheFields() {
      if (this.noCacheFields == null) {
         this.noCacheFields = new ArrayList();
      }

      return this.noCacheFields;
   }

   public void setNoCache(boolean noCache) {
      this.noCache = noCache;
   }

   public boolean isNoCache() {
      return this.noCache;
   }

   public boolean isPrivate() {
      return this.privateFlag;
   }

   public List<String> getPrivateFields() {
      if (this.privateFields == null) {
         this.privateFields = new ArrayList();
      }

      return this.privateFields;
   }

   public void setPrivate(boolean flag) {
      this.privateFlag = flag;
   }

   public boolean isNoTransform() {
      return this.noTransform;
   }

   public void setNoTransform(boolean noTransform) {
      this.noTransform = noTransform;
   }

   public boolean isNoStore() {
      return this.noStore;
   }

   public void setNoStore(boolean noStore) {
      this.noStore = noStore;
   }

   public Map<String, String> getCacheExtension() {
      if (this.cacheExtension == null) {
         this.cacheExtension = new HashMap();
      }

      return this.cacheExtension;
   }

   public String toString() {
      return HEADER_DELEGATE.toString(this);
   }

   public int hashCode() {
      int hash = 7;
      hash = 41 * hash + (this.privateFlag ? 1 : 0);
      hash = 41 * hash + (this.noCache ? 1 : 0);
      hash = 41 * hash + (this.noStore ? 1 : 0);
      hash = 41 * hash + (this.noTransform ? 1 : 0);
      hash = 41 * hash + (this.mustRevalidate ? 1 : 0);
      hash = 41 * hash + (this.proxyRevalidate ? 1 : 0);
      hash = 41 * hash + this.maxAge;
      hash = 41 * hash + this.sMaxAge;
      hash = 41 * hash + hashCodeOf((Collection)this.privateFields);
      hash = 41 * hash + hashCodeOf((Collection)this.noCacheFields);
      hash = 41 * hash + hashCodeOf(this.cacheExtension);
      return hash;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         CacheControl other = (CacheControl)obj;
         if (this.privateFlag != other.privateFlag) {
            return false;
         } else if (this.noCache != other.noCache) {
            return false;
         } else if (this.noStore != other.noStore) {
            return false;
         } else if (this.noTransform != other.noTransform) {
            return false;
         } else if (this.mustRevalidate != other.mustRevalidate) {
            return false;
         } else if (this.proxyRevalidate != other.proxyRevalidate) {
            return false;
         } else if (this.maxAge != other.maxAge) {
            return false;
         } else if (this.sMaxAge != other.sMaxAge) {
            return false;
         } else if (notEqual((Collection)this.privateFields, (Collection)other.privateFields)) {
            return false;
         } else if (notEqual((Collection)this.noCacheFields, (Collection)other.noCacheFields)) {
            return false;
         } else {
            return !notEqual(this.cacheExtension, other.cacheExtension);
         }
      }
   }

   private static boolean notEqual(Collection<?> first, Collection<?> second) {
      if (first == second) {
         return false;
      } else if (first == null) {
         return !second.isEmpty();
      } else if (second == null) {
         return !first.isEmpty();
      } else {
         return !first.equals(second);
      }
   }

   private static boolean notEqual(Map<?, ?> first, Map<?, ?> second) {
      if (first == second) {
         return false;
      } else if (first == null) {
         return !second.isEmpty();
      } else if (second == null) {
         return !first.isEmpty();
      } else {
         return !first.equals(second);
      }
   }

   private static int hashCodeOf(Collection<?> instance) {
      return instance != null && !instance.isEmpty() ? instance.hashCode() : 0;
   }

   private static int hashCodeOf(Map<?, ?> instance) {
      return instance != null && !instance.isEmpty() ? instance.hashCode() : 0;
   }
}
