package javax.ws.rs.core;

import java.util.Map;

public interface Configurable<C extends Configurable> {
   Configuration getConfiguration();

   C property(String var1, Object var2);

   C register(Class<?> var1);

   C register(Class<?> var1, int var2);

   C register(Class<?> var1, Class... var2);

   C register(Class<?> var1, Map<Class<?>, Integer> var2);

   C register(Object var1);

   C register(Object var1, int var2);

   C register(Object var1, Class... var2);

   C register(Object var1, Map<Class<?>, Integer> var2);
}
