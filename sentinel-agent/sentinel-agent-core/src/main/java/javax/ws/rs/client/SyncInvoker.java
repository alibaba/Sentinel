package javax.ws.rs.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public interface SyncInvoker {
   Response get();

   <T> T get(Class<T> var1);

   <T> T get(GenericType<T> var1);

   Response put(Entity<?> var1);

   <T> T put(Entity<?> var1, Class<T> var2);

   <T> T put(Entity<?> var1, GenericType<T> var2);

   Response post(Entity<?> var1);

   <T> T post(Entity<?> var1, Class<T> var2);

   <T> T post(Entity<?> var1, GenericType<T> var2);

   Response delete();

   <T> T delete(Class<T> var1);

   <T> T delete(GenericType<T> var1);

   Response head();

   Response options();

   <T> T options(Class<T> var1);

   <T> T options(GenericType<T> var1);

   Response trace();

   <T> T trace(Class<T> var1);

   <T> T trace(GenericType<T> var1);

   Response method(String var1);

   <T> T method(String var1, Class<T> var2);

   <T> T method(String var1, GenericType<T> var2);

   Response method(String var1, Entity<?> var2);

   <T> T method(String var1, Entity<?> var2, Class<T> var3);

   <T> T method(String var1, Entity<?> var2, GenericType<T> var3);
}
