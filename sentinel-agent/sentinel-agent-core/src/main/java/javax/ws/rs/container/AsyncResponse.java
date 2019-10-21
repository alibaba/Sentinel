package javax.ws.rs.container;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface AsyncResponse {
   long NO_TIMEOUT = 0L;

   boolean resume(Object var1);

   boolean resume(Throwable var1);

   boolean cancel();

   boolean cancel(int var1);

   boolean cancel(Date var1);

   boolean isSuspended();

   boolean isCancelled();

   boolean isDone();

   boolean setTimeout(long var1, TimeUnit var3);

   void setTimeoutHandler(TimeoutHandler var1);

   Collection<Class<?>> register(Class<?> var1);

   Map<Class<?>, Collection<Class<?>>> register(Class<?> var1, Class... var2);

   Collection<Class<?>> register(Object var1);

   Map<Class<?>, Collection<Class<?>>> register(Object var1, Object... var2);
}
