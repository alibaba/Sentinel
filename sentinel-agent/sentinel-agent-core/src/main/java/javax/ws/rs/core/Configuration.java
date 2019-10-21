package javax.ws.rs.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.RuntimeType;

public interface Configuration {
   RuntimeType getRuntimeType();

   Map<String, Object> getProperties();

   Object getProperty(String var1);

   Collection<String> getPropertyNames();

   boolean isEnabled(Feature var1);

   boolean isEnabled(Class<? extends Feature> var1);

   boolean isRegistered(Object var1);

   boolean isRegistered(Class<?> var1);

   Map<Class<?>, Integer> getContracts(Class<?> var1);

   Set<Class<?>> getClasses();

   Set<Object> getInstances();
}
