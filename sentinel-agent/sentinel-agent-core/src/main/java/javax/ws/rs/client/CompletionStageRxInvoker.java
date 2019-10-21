package javax.ws.rs.client;

import java.util.concurrent.CompletionStage;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public interface CompletionStageRxInvoker extends RxInvoker<CompletionStage> {
   CompletionStage<Response> get();

   <T> CompletionStage<T> get(Class<T> var1);

   <T> CompletionStage<T> get(GenericType<T> var1);

   CompletionStage<Response> put(Entity<?> var1);

   <T> CompletionStage<T> put(Entity<?> var1, Class<T> var2);

   <T> CompletionStage<T> put(Entity<?> var1, GenericType<T> var2);

   CompletionStage<Response> post(Entity<?> var1);

   <T> CompletionStage<T> post(Entity<?> var1, Class<T> var2);

   <T> CompletionStage<T> post(Entity<?> var1, GenericType<T> var2);

   CompletionStage<Response> delete();

   <T> CompletionStage<T> delete(Class<T> var1);

   <T> CompletionStage<T> delete(GenericType<T> var1);

   CompletionStage<Response> head();

   CompletionStage<Response> options();

   <T> CompletionStage<T> options(Class<T> var1);

   <T> CompletionStage<T> options(GenericType<T> var1);

   CompletionStage<Response> trace();

   <T> CompletionStage<T> trace(Class<T> var1);

   <T> CompletionStage<T> trace(GenericType<T> var1);

   CompletionStage<Response> method(String var1);

   <T> CompletionStage<T> method(String var1, Class<T> var2);

   <T> CompletionStage<T> method(String var1, GenericType<T> var2);

   CompletionStage<Response> method(String var1, Entity<?> var2);

   <T> CompletionStage<T> method(String var1, Entity<?> var2, Class<T> var3);

   <T> CompletionStage<T> method(String var1, Entity<?> var2, GenericType<T> var3);
}
