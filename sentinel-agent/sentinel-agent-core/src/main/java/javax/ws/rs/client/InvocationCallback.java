package javax.ws.rs.client;

public interface InvocationCallback<RESPONSE> {
   void completed(RESPONSE var1);

   void failed(Throwable var1);
}
