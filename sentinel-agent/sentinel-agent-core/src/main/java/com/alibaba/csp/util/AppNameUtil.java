package com.alibaba.csp.util;//package com.taobao.csp.ahas.util;
//
//import com.alibaba.csp.sentinel.log.RecordLog;
//
//import java.io.File;
//
//public final class AppNameUtil {
//   public static final String APP_NAME = "project.name";
//   public static final String SUN_JAVA_COMMAND = "sun.java.command";
//   private static final String JAR_SUFFIX_LOWER = ".jar";
//   private static final String JAR_SUFFIX_UPPER = ".JAR";
//   private static String appName;
//
//   private AppNameUtil() {
//   }
//
//   public static void resolveAppName() {
//      String app = System.getProperty("project.name");
//      if (!isEmpty(app)) {
//         appName = app;
//      } else {
//         String command = System.getProperty("sun.java.command");
//         if (!isEmpty(command)) {
//            command = command.split("\\s")[0];
//            String separator = File.separator;
//            if (command.contains(separator)) {
//               String[] strs;
//               if ("\\".equals(separator)) {
//                  strs = command.split("\\\\");
//               } else {
//                  strs = command.split(separator);
//               }
//
//               command = strs[strs.length - 1];
//            }
//
//            if (command.endsWith(".jar") || command.endsWith(".JAR")) {
//               command = command.substring(0, command.length() - 4);
//            }
//
//            appName = command;
//         }
//      }
//   }
//
//   public static String getAppName() {
//      return appName;
//   }
//
//   private static boolean isEmpty(String str) {
//      return str == null || "".equals(str);
//   }
//
//   static {
//      resolveAppName();
//      RecordLog.info("App name resolved: " + appName);
//   }
//}
