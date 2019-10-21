package com.taobao.csp.ahas.service.util;

import java.io.File;

public final class AppNameUtil {
   public static final int APP_TYPE_COMMON = 0;
   public static final String APP_TYPE = "csp.sentinel.app.type";
   public static final String APP_NAME = "project.name";
   public static final String AHAS_APP_NAME = "ahas.project.name";
   public static final String SUN_JAVA_COMMAND = "sun.java.command";
   private static final String JAR_SUFFIX_LOWER = ".jar";
   private static final String JAR_SUFFIX_UPPER = ".JAR";
   private static String appName;
   private static String ahasAppName;
   private static int appType;

   private AppNameUtil() {
   }

   public static void resolveAppType() {
      try {
         String type = System.getProperty("csp.sentinel.app.type");
         if (type == null) {
            appType = 0;
            return;
         }

         appType = Integer.parseInt(type);
         if (appType < 0) {
            appType = 0;
         }
      } catch (Exception var1) {
         appType = 0;
      }

   }

   public static void resolveAhasAppName() {
      String app = System.getProperty("ahas.project.name");
      if (!isEmpty(app)) {
         ahasAppName = app;
      }

   }

   public static void resolveAppName() {
      String app = System.getProperty("project.name");
      if (!isEmpty(app)) {
         appName = app;
      } else {
         String command = System.getProperty("sun.java.command");
         if (!isEmpty(command)) {
            command = command.split("\\s")[0];
            String separator = File.separator;
            if (command.contains(separator)) {
               String[] strs;
               if ("\\".equals(separator)) {
                  strs = command.split("\\\\");
               } else {
                  strs = command.split(separator);
               }

               command = strs[strs.length - 1];
            }

            if (command.endsWith(".jar") || command.endsWith(".JAR")) {
               command = command.substring(0, command.length() - 4);
            }

            appName = command;
         }
      }
   }

   public static String getAppName() {
      return appName;
   }

   public static String getAhasAppName() {
      return ahasAppName;
   }

   public static int getAppType() {
      return appType;
   }

   private static boolean isEmpty(String str) {
      return str == null || "".equals(str);
   }

   static {
      resolveAppName();
      resolveAppType();
      resolveAhasAppName();
   }
}
