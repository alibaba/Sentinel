package javax.ws.rs.client;

import javax.ws.rs.core.GenericType;

public interface RxInvoker<T> {
   T get();

   <R> T get(Class<R> var1);

   <R> T get(GenericType<R> var1);

   T put(Entity<?> var1);

   <R> T put(Entity<?> var1, Class<R> var2);

   <R> T put(Entity<?> var1, GenericType<R> var2);

   T post(Entity<?> var1);

   <R> T post(Entity<?> var1, Class<R> var2);

   <R> T post(Entity<?> var1, GenericType<R> var2);

   T delete();

   <R> T delete(Class<R> var1);

   <R> T delete(GenericType<R> var1);

   T head();

   T options();

   <R> T options(Class<R> var1);

   <R> T options(GenericType<R> var1);

   T trace();

   <R> T trace(Class<R> var1);

   <R> T trace(GenericType<R> var1);

   T method(String var1);

   <R> T method(String var1, Class<R> var2);

   <R> T method(String var1, GenericType<R> var2);

   T method(String var1, Entity<?> var2);

   <R> T method(String var1, Entity<?> var2, Class<R> var3);

   <R> T method(String var1, Entity<?> var2, GenericType<R> var3);
}
