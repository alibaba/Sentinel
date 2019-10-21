package com.alibaba.csp.util;//package com.taobao.csp.ahas.util;
//
//import com.alibaba.csp.sentinel.log.RecordLog;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Enumeration;
//
//public final class HostNameUtil {
//   private static String ip;
//   private static String hostName;
//
//   private static void resolveHost() throws Exception {
//      InetAddress addr = InetAddress.getLocalHost();
//      hostName = addr.getHostName();
//      ip = addr.getHostAddress();
//      if (addr.isLoopbackAddress()) {
//         Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
//
//         while(interfaces.hasMoreElements()) {
//            NetworkInterface in = (NetworkInterface)interfaces.nextElement();
//            Enumeration addrs = in.getInetAddresses();
//
//            while(addrs.hasMoreElements()) {
//               InetAddress address = (InetAddress)addrs.nextElement();
//               if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
//                  ip = address.getHostAddress();
//               }
//            }
//         }
//      }
//
//   }
//
//   public static String getIp() {
//      return ip;
//   }
//
//   public static String getHostName() {
//      return hostName;
//   }
//
//   public static String getConfigString() {
//      return "{\n\t\"machine\": \"" + hostName + "\",\n\t\"ip\": \"" + ip + "\"\n}";
//   }
//
//   private HostNameUtil() {
//   }
//
//   static {
//      try {
//         resolveHost();
//      } catch (Exception var1) {
//         RecordLog.info("Failed to get local host", (Throwable)var1);
//      }
//
//   }
//}
