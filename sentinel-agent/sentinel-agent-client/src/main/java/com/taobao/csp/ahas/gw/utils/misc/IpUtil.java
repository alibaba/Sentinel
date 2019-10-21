package com.taobao.csp.ahas.gw.utils.misc;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {
   private static String ip = getIp();
   private static String address = getAddress();

   public static String getIp() {
      if (ip == null) {
         try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
            address = addr.getHostName();
         } catch (Exception var1) {
            return null;
         }
      }

      return ip;
   }

   public static String getAddress() {
      if (address == null) {
         try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
            address = addr.getHostName();
         } catch (UnknownHostException var2) {
            return null;
         }
      }

      return address;
   }

   public static long ipToLong(String strIp) {
      String[] ip = strIp.split("\\.");
      return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
   }

   public static String longToIP(long longIp) {
      StringBuffer sb = new StringBuffer("");
      sb.append(String.valueOf(longIp >>> 24));
      sb.append(".");
      sb.append(String.valueOf((longIp & 16777215L) >>> 16));
      sb.append(".");
      sb.append(String.valueOf((longIp & 65535L) >>> 8));
      sb.append(".");
      sb.append(String.valueOf(longIp & 255L));
      return sb.toString();
   }
}
