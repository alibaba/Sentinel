package com.taobao.csp.ahas.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VpcEcsUtil {
   private static final String vpcMetaUrl = System.getProperty("ahas.vpc.url", "http://100.100.100.200/latest/meta-data/");

   public static boolean isVpcEnv() {
      return getRemoteMessage(vpcMetaUrl) != null;
   }

   public static String getVpcId() {
      return getRemoteMessage(vpcMetaUrl + "vpc-id");
   }

   public static String getPrivateIpv4() {
      return getRemoteMessage(vpcMetaUrl + "private-ipv4");
   }

   public static String getInstanceId() {
      return getRemoteMessage(vpcMetaUrl + "instance-id");
   }

   public static String getRegionId() {
      return getRemoteMessage(vpcMetaUrl + "region-id");
   }

   public static String getUid() {
      return getRemoteMessage(vpcMetaUrl + "owner-account-id");
   }

   private static String getRemoteMessage(String urlValue) {
      HttpURLConnection conn = null;
      BufferedReader in = null;

      try {
         URL url = new URL(urlValue);
         conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         conn.setConnectTimeout(1000);
         conn.setReadTimeout(2000);
         conn.setInstanceFollowRedirects(true);
         in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         StringBuffer content = new StringBuffer();

         String inputLine;
         while((inputLine = in.readLine()) != null) {
            content.append(inputLine);
         }

         String var6 = content.toString();
         return var6;
      } catch (Exception var16) {
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException var15) {
            }
         }

         if (conn != null) {
            conn.disconnect();
         }

      }

      return null;
   }
}
