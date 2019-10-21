package javax.ws.rs.container;

import java.lang.reflect.Method;

public interface ResourceInfo {
   Method getResourceMethod();

   Class<?> getResourceClass();
}
