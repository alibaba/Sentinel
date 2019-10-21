package javax.ws.rs.client;

import java.util.concurrent.ExecutorService;

public interface RxInvokerProvider<T extends RxInvoker> {
   boolean isProviderFor(Class<?> var1);

   T getRxInvoker(SyncInvoker var1, ExecutorService var2);
}
