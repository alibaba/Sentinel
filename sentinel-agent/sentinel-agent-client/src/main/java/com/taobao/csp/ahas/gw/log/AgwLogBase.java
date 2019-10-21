package com.taobao.csp.ahas.gw.log;

import com.taobao.csp.ahas.gw.utils.misc.AgwPidUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgwLogBase {
   public static final String LOG_CHARSET = "utf-8";
   private static final String DIR_NAME;
   private static final String USER_HOME = "user.home";
   public static final String LOG_DIR = "csp.sentinel.log.dir";
   public static final String LOG_NAME_USE_PID = "csp.sentinel.log.use.pid";
   private static boolean logNameUsePid;
   private static String logBaseDir;

   private static void init() {
      String logDir = System.getProperty("csp.sentinel.log.dir");
      if (logDir == null || logDir.isEmpty()) {
         logDir = System.getProperty("user.home");
         logDir = addSeparator(logDir) + DIR_NAME + File.separator;
      }

      logDir = addSeparator(logDir);
      File dir = new File(logDir);
      if (!dir.exists() && !dir.mkdirs()) {
         System.err.println("ERROR: create log base dir error: " + logDir);
      }

      logBaseDir = logDir;
      System.out.println("AGW INFO: log base dir is: " + logBaseDir);
      String usePid = System.getProperty("csp.sentinel.log.use.pid", "");
      logNameUsePid = "true".equalsIgnoreCase(usePid);
      System.out.println("AGW INFO: log name use pid is: " + logNameUsePid);
   }

   public static boolean isLogNameUsePid() {
      return logNameUsePid;
   }

   private static String addSeparator(String logDir) {
      if (!logDir.endsWith(File.separator)) {
         logDir = logDir + File.separator;
      }

      return logDir;
   }

   protected static void log(Logger logger, Handler handler, Level level, String detail, Object... params) {
      if (detail != null) {
         AgwLoggerUtils.disableOtherHandlers(logger, handler);
         if (params.length == 0) {
            logger.log(level, detail);
         } else {
            logger.log(level, detail, params);
         }

      }
   }

   protected static void log(Logger logger, Handler handler, Level level, String detail, Throwable throwable) {
      if (detail != null) {
         AgwLoggerUtils.disableOtherHandlers(logger, handler);
         logger.log(level, detail, throwable);
      }
   }

   public static String getLogBaseDir() {
      return logBaseDir;
   }

   protected static Handler makeLogger(String logName, Logger heliumRecordLog) {
      AgwCspFormatter formatter = new AgwCspFormatter();
      String fileName = getLogBaseDir() + logName;
      if (isLogNameUsePid()) {
         fileName = fileName + ".pid" + AgwPidUtil.getPid();
      }

      AgwDateFileLogHandler handler = null;

      try {
         handler = new AgwDateFileLogHandler(fileName + ".%d", 209715200, 4, true);
         handler.setFormatter(formatter);
         handler.setEncoding("utf-8");
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      if (handler != null) {
         AgwLoggerUtils.disableOtherHandlers(heliumRecordLog, handler);
      }

      heliumRecordLog.setLevel(Level.ALL);
      return handler;
   }

   static {
      DIR_NAME = "logs" + File.separator + "csp";
      logNameUsePid = false;

      try {
         init();
      } catch (Throwable var1) {
         var1.printStackTrace();
         System.exit(-1);
      }

   }
}
