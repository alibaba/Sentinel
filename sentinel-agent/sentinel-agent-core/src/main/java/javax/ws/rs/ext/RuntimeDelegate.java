package javax.ws.rs.ext;

import java.lang.reflect.ReflectPermission;
import java.net.URL;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;

public abstract class RuntimeDelegate {
   public static final String JAXRS_RUNTIME_DELEGATE_PROPERTY = "javax.ws.rs.ext.RuntimeDelegate";
   private static final String JAXRS_DEFAULT_RUNTIME_DELEGATE = "org.glassfish.jersey.internal.RuntimeDelegateImpl";
   private static final Object RD_LOCK = new Object();
   private static ReflectPermission suppressAccessChecksPermission = new ReflectPermission("suppressAccessChecks");
   private static volatile RuntimeDelegate cachedDelegate;

   protected RuntimeDelegate() {
   }

   public static RuntimeDelegate getInstance() {
      RuntimeDelegate result = cachedDelegate;
      if (result == null) {
         synchronized(RD_LOCK) {
            result = cachedDelegate;
            if (result == null) {
               cachedDelegate = result = findDelegate();
            }
         }
      }

      return result;
   }

   private static RuntimeDelegate findDelegate() {
      try {
         Object delegate = FactoryFinder.find("javax.ws.rs.ext.RuntimeDelegate", "org.glassfish.jersey.internal.RuntimeDelegateImpl", RuntimeDelegate.class);
         if (!(delegate instanceof RuntimeDelegate)) {
            Class pClass = RuntimeDelegate.class;
            String classnameAsResource = pClass.getName().replace('.', '/') + ".class";
            ClassLoader loader = pClass.getClassLoader();
            if (loader == null) {
               loader = ClassLoader.getSystemClassLoader();
            }

            URL targetTypeURL = loader.getResource(classnameAsResource);
            throw new LinkageError("ClassCastException: attempting to cast" + delegate.getClass().getClassLoader().getResource(classnameAsResource) + " to " + targetTypeURL);
         } else {
            return (RuntimeDelegate)delegate;
         }
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }
   }

   public static void setInstance(RuntimeDelegate rd) {
      SecurityManager security = System.getSecurityManager();
      if (security != null) {
         security.checkPermission(suppressAccessChecksPermission);
      }

      synchronized(RD_LOCK) {
         cachedDelegate = rd;
      }
   }

   public abstract UriBuilder createUriBuilder();

   public abstract Response.ResponseBuilder createResponseBuilder();

   public abstract Variant.VariantListBuilder createVariantListBuilder();

   public abstract <T> T createEndpoint(Application var1, Class<T> var2) throws IllegalArgumentException, UnsupportedOperationException;

   public abstract <T> HeaderDelegate<T> createHeaderDelegate(Class<T> var1) throws IllegalArgumentException;

   public abstract Link.Builder createLinkBuilder();

   public interface HeaderDelegate<T> {
      T fromString(String var1);

      String toString(T var1);
   }
}
