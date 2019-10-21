package javax.ws.rs.ext;

public interface ContextResolver<T> {
   T getContext(Class<?> var1);
}
