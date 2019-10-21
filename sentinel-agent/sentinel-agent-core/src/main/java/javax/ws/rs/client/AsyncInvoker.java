package javax.ws.rs.client;

import java.util.concurrent.Future;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public interface AsyncInvoker {
   Future<Response> get();

   <T> Future<T> get(Class<T> var1);

   <T> Future<T> get(GenericType<T> var1);

   <T> Future<T> get(InvocationCallback<T> var1);

   Future<Response> put(Entity<?> var1);

   <T> Future<T> put(Entity<?> var1, Class<T> var2);

   <T> Future<T> put(Entity<?> var1, GenericType<T> var2);

   <T> Future<T> put(Entity<?> var1, InvocationCallback<T> var2);

   Future<Response> post(Entity<?> var1);

   <T> Future<T> post(Entity<?> var1, Class<T> var2);

   <T> Future<T> post(Entity<?> var1, GenericType<T> var2);

   <T> Future<T> post(Entity<?> var1, InvocationCallback<T> var2);

   Future<Response> delete();

   <T> Future<T> delete(Class<T> var1);

   <T> Future<T> delete(GenericType<T> var1);

   <T> Future<T> delete(InvocationCallback<T> var1);

   Future<Response> head();

   Future<Response> head(InvocationCallback<Response> var1);

   Future<Response> options();

   <T> Future<T> options(Class<T> var1);

   <T> Future<T> options(GenericType<T> var1);

   <T> Future<T> options(InvocationCallback<T> var1);

   Future<Response> trace();

   <T> Future<T> trace(Class<T> var1);

   <T> Future<T> trace(GenericType<T> var1);

   <T> Future<T> trace(InvocationCallback<T> var1);

   Future<Response> method(String var1);

   <T> Future<T> method(String var1, Class<T> var2);

   <T> Future<T> method(String var1, GenericType<T> var2);

   <T> Future<T> method(String var1, InvocationCallback<T> var2);

   Future<Response> method(String var1, Entity<?> var2);

   <T> Future<T> method(String var1, Entity<?> var2, Class<T> var3);

   <T> Future<T> method(String var1, Entity<?> var2, GenericType<T> var3);

   <T> Future<T> method(String var1, Entity<?> var2, InvocationCallback<T> var3);
}
