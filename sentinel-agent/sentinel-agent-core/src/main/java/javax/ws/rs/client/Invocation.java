package javax.ws.rs.client;

import java.util.Locale;
import java.util.concurrent.Future;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public interface Invocation {
   Invocation property(String var1, Object var2);

   Response invoke();

   <T> T invoke(Class<T> var1);

   <T> T invoke(GenericType<T> var1);

   Future<Response> submit();

   <T> Future<T> submit(Class<T> var1);

   <T> Future<T> submit(GenericType<T> var1);

   <T> Future<T> submit(InvocationCallback<T> var1);

   public interface Builder extends SyncInvoker {
      Invocation build(String var1);

      Invocation build(String var1, Entity<?> var2);

      Invocation buildGet();

      Invocation buildDelete();

      Invocation buildPost(Entity<?> var1);

      Invocation buildPut(Entity<?> var1);

      AsyncInvoker async();

      Builder accept(String... var1);

      Builder accept(MediaType... var1);

      Builder acceptLanguage(Locale... var1);

      Builder acceptLanguage(String... var1);

      Builder acceptEncoding(String... var1);

      Builder cookie(Cookie var1);

      Builder cookie(String var1, String var2);

      Builder cacheControl(CacheControl var1);

      Builder header(String var1, Object var2);

      Builder headers(MultivaluedMap<String, Object> var1);

      Builder property(String var1, Object var2);

      CompletionStageRxInvoker rx();

      <T extends RxInvoker> T rx(Class<T> var1);
   }
}
