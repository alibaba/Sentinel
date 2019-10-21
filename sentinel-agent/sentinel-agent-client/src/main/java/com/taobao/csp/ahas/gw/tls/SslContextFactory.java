package com.taobao.csp.ahas.gw.tls;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SslContextFactory {
   private static final String PROTOCOL = "TLS";
   private static SSLContext SERVER_CONTEXT;
   private static SSLContext CLIENT_CONTEXT;

   public static SSLContext getServerContext(String pkPath, String caPath) {
      if (SERVER_CONTEXT != null) {
         return SERVER_CONTEXT;
      } else {
         FileInputStream in = null;

         try {
            KeyManagerFactory kmf = null;
            if (pkPath != null) {
               KeyStore ks = KeyStore.getInstance("JKS");
               in = new FileInputStream(pkPath);
               ks.load(in, "sNetty".toCharArray());
               kmf = KeyManagerFactory.getInstance("SunX509");
               kmf.init(ks, "sNetty".toCharArray());
            }

            SERVER_CONTEXT = SSLContext.getInstance("TLS");
            SERVER_CONTEXT.init(kmf.getKeyManagers(), (TrustManager[])null, (SecureRandom)null);
         } catch (Exception var12) {
            throw new Error("Failed to initialize the server-side SSLContext", var12);
         } finally {
            if (in != null) {
               try {
                  in.close();
               } catch (IOException var11) {
                  var11.printStackTrace();
               }

               in = null;
            }

         }

         return SERVER_CONTEXT;
      }
   }

   public static SSLContext getClientContext(String pkPath, String caPath) {
      if (CLIENT_CONTEXT != null) {
         return CLIENT_CONTEXT;
      } else {
         FileInputStream tIN = null;

         try {
            TrustManagerFactory tf = null;
            if (caPath != null) {
               KeyStore tks = KeyStore.getInstance("JKS");
               tIN = new FileInputStream(caPath);
               tks.load(tIN, "sNetty".toCharArray());
               tf = TrustManagerFactory.getInstance("SunX509");
               tf.init(tks);
            }

            CLIENT_CONTEXT = SSLContext.getInstance("TLS");
            CLIENT_CONTEXT.init((KeyManager[])null, tf.getTrustManagers(), (SecureRandom)null);
         } catch (Exception var12) {
            var12.printStackTrace();
            throw new Error("Failed to initialize the client-side SSLContext");
         } finally {
            if (tIN != null) {
               try {
                  tIN.close();
               } catch (IOException var11) {
                  var11.printStackTrace();
               }

               tIN = null;
            }

         }

         return CLIENT_CONTEXT;
      }
   }
}
