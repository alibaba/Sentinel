package com.taobao.csp.ahas.gw.oss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class OssCert {
   public static Map<String, String> ossServerAddress = new HashMap();
   public static Map<String, String> ossClientAddress = new HashMap();
   public static String serverCertPath = "/tmp/sChat.jks";
   public static String clientCertPath = "/tmp/cChat.jks";

   public static void createOrUpdateServerCert(String regionId, boolean isProduct) {
      String key = null;
      if (isProduct) {
         key = "prod-" + regionId;
      } else {
         key = "pre-" + regionId;
      }

      String ossAddress = (String)ossServerAddress.get(key);
      download(serverCertPath, ossAddress);
   }

   public static void createOrUpdateClientCert(String regionId, String env) {
      String key = env + "-" + regionId;
      String ossAddress = (String)ossClientAddress.get(key);
      download(clientCertPath, ossAddress);
   }

   public static void download(String filePath, String ossAddress) {
      FileOutputStream out = null;
      InputStream in = null;

      try {
         URL url = new URL(ossAddress);
         URLConnection urlConnection = url.openConnection();
         HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
         httpURLConnection.setDoOutput(true);
         httpURLConnection.setDoInput(true);
         httpURLConnection.setUseCaches(false);
         httpURLConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
         httpURLConnection.setRequestMethod("GET");
         httpURLConnection.setRequestProperty("connection", "Keep-Alive");
         httpURLConnection.setRequestProperty("Charsert", "UTF-8");
         httpURLConnection.setConnectTimeout(60000);
         httpURLConnection.setReadTimeout(60000);
         httpURLConnection.connect();
         in = httpURLConnection.getInputStream();
         File file = new File(filePath);
         File fileParent = file.getParentFile();
         if (!fileParent.exists()) {
            fileParent.mkdirs();
         }

         if (!file.exists()) {
            file.createNewFile();
         }

         out = new FileOutputStream(file);
         byte[] buffer = new byte[4096];
         boolean var10 = false;

         int readLength;
         while((readLength = in.read(buffer)) > 0) {
            byte[] bytes = new byte[readLength];
            System.arraycopy(buffer, 0, bytes, 0, readLength);
            out.write(bytes);
         }

         out.flush();
      } catch (Exception var24) {
         var24.printStackTrace();
      } finally {
         try {
            if (in != null) {
               in.close();
            }
         } catch (IOException var23) {
            var23.printStackTrace();
         }

         try {
            if (out != null) {
               out.close();
            }
         } catch (IOException var22) {
            var22.printStackTrace();
         }

      }

   }

   public static void main(String[] args) {
      download("/tmp/sChat.jks", "https://ahas-cn-public.oss-cn-hangzhou.aliyuncs.com/server/cert/sChat.jks");
   }

   static {
      ossServerAddress.put("test-cn-public", "https://ahas-cn-public.oss-cn-hangzhou.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("pre-cn-public", "https://ahas-cn-public.oss-cn-hangzhou.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("prod-cn-public", "https://ahasoss-cn-public.oss-cn-hangzhou.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("test-cn-hangzhou", "https://ahas-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("pre-cn-hangzhou", "https://ahas-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("prod-cn-hangzhou", "https://ahasoss-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("prod-cn-beijing", "https://ahasoss-cn-beijing.oss-cn-beijing-internal.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("prod-cn-shanghai", "https://ahasoss-cn-shanghai.oss-cn-shanghai-internal.aliyuncs.com/server/cert/sChat.jks");
      ossServerAddress.put("prod-cn-shenzhen", "https://ahasoss-cn-shenzhen.oss-cn-shenzhen-internal.aliyuncs.com/server/cert/sChat.jks");
      ossClientAddress.put("test-cn-public", "https://ahas-cn-public.oss-cn-hangzhou.aliyuncs.com/agent/test/cert/cChat.jks");
      ossClientAddress.put("pre-cn-public", "https://ahas-cn-public.oss-cn-hangzhou.aliyuncs.com/agent/pre/cert/cChat.jks");
      ossClientAddress.put("prod-cn-public", "https://ahasoss-cn-public.oss-cn-hangzhou.aliyuncs.com/agent/prod/cert/cChat.jks");
      ossClientAddress.put("test-cn-hangzhou", "https://ahas-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/agent/test/cert/cChat.jks");
      ossClientAddress.put("pre-cn-hangzhou", "https://ahas-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/agent/pre/cert/cChat.jks");
      ossClientAddress.put("prod-cn-hangzhou", "https://ahasoss-cn-hangzhou.oss-cn-hangzhou-internal.aliyuncs.com/agent/prod/cert/cChat.jks");
      ossClientAddress.put("prod-cn-beijing", "https://ahasoss-cn-beijing.oss-cn-beijing-internal.aliyuncs.com/agent/prod/cert/cChat.jks");
      ossClientAddress.put("prod-cn-shanghai", "https://ahasoss-cn-shanghai.oss-cn-shanghai-internal.aliyuncs.com/agent/prod/cert/cChat.jks");
      ossClientAddress.put("prod-cn-shenzhen", "https://ahasoss-cn-shenzhen.oss-cn-shenzhen-internal.aliyuncs.com/agent/prod/cert/cChat.jks");
   }
}
