package javax.ws.rs.client;

import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

public abstract class ClientBuilder implements Configurable<ClientBuilder> {
   public static final String JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY = "javax.ws.rs.client.ClientBuilder";
   private static final String JAXRS_DEFAULT_CLIENT_BUILDER = "org.glassfish.jersey.client.JerseyClientBuilder";

   protected ClientBuilder() {
   }

   public static ClientBuilder newBuilder() {
      try {
         Object delegate = FactoryFinder.find("javax.ws.rs.client.ClientBuilder", "org.glassfish.jersey.client.JerseyClientBuilder", ClientBuilder.class);
         if (!(delegate instanceof ClientBuilder)) {
            Class pClass = ClientBuilder.class;
            String classnameAsResource = pClass.getName().replace('.', '/') + ".class";
            ClassLoader loader = pClass.getClassLoader();
            if (loader == null) {
               loader = ClassLoader.getSystemClassLoader();
            }

            URL targetTypeURL = loader.getResource(classnameAsResource);
            throw new LinkageError("ClassCastException: attempting to cast" + delegate.getClass().getClassLoader().getResource(classnameAsResource) + " to " + targetTypeURL);
         } else {
            return (ClientBuilder)delegate;
         }
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }
   }

   public static Client newClient() {
      return newBuilder().build();
   }

   public static Client newClient(Configuration configuration) {
      return newBuilder().withConfig(configuration).build();
   }

   public abstract ClientBuilder withConfig(Configuration var1);

   public abstract ClientBuilder sslContext(SSLContext var1);

   public abstract ClientBuilder keyStore(KeyStore var1, char[] var2);

   public ClientBuilder keyStore(KeyStore keyStore, String password) {
      return this.keyStore(keyStore, password.toCharArray());
   }

   public abstract ClientBuilder trustStore(KeyStore var1);

   public abstract ClientBuilder hostnameVerifier(HostnameVerifier var1);

   public abstract ClientBuilder executorService(ExecutorService var1);

   public abstract ClientBuilder scheduledExecutorService(ScheduledExecutorService var1);

   public abstract ClientBuilder connectTimeout(long var1, TimeUnit var3);

   public abstract ClientBuilder readTimeout(long var1, TimeUnit var3);

   public abstract Client build();
}
