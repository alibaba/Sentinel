package com.alibaba.csp.util;

public class EnvUtil {
   public static String getVersion() {
      String version = System.getProperty("java.version");
      if (StringUtil.isBlank(version)) {
         version = System.getProperty("java.specification.version");
      }

      return version.replaceAll("_", ".");
   }

   public static boolean isJdk6OrHigher() {
      return isJdkNOrHigher(1.6D);
   }

   public static boolean isJDK7OrHigher() {
      return isJdkNOrHigher(1.7D);
   }

   private static boolean isJdkNOrHigher(double ver) {
      String version = getVersion();
      String[] strings = version.split("\\.");
      int tokenSize = 2;
      if (strings.length > tokenSize) {
         StringBuilder builder = new StringBuilder();
         builder.append(strings[0]).append(".").append(strings[1]);
         version = builder.toString();
      }

      int i = Double.compare(Double.parseDouble(version), ver);
      return i >= 0;
   }

   public static boolean isWindowsPlatform() {
      String os = System.getProperty("os.name").toLowerCase();
      return os.indexOf("windows") >= 0;
   }
}
