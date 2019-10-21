package javax.ws.rs.container;

public interface ResourceContext {
   <T> T getResource(Class<T> var1);

   <T> T initResource(T var1);
}
