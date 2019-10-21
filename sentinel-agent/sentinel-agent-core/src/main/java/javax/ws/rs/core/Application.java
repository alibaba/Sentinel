package javax.ws.rs.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Application {
   public Set<Class<?>> getClasses() {
      return Collections.emptySet();
   }

   public Set<Object> getSingletons() {
      return Collections.emptySet();
   }

   public Map<String, Object> getProperties() {
      return Collections.emptyMap();
   }
}
