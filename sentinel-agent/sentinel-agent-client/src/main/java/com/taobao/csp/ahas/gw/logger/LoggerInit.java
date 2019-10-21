package com.taobao.csp.ahas.gw.logger;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;
import com.taobao.middleware.logger.support.LoggerHelper;

public class LoggerInit {
   public static final Logger ORIGIN_LOGGER = LoggerFactory.getLogger("taobao.agw");
   public static final Logger LOGGER;
   public static final Logger LOGGER_PERF;

   private static void initAGWLogWithSizeRolling(int maxBackupIndex, String fileSize) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      try {
         Thread.currentThread().setContextClassLoader(LoggerInit.class.getClassLoader());
         int fileSizeInMB = Integer.valueOf(fileSize.substring(0, fileSize.length() - 2));
         String sizeType = fileSize.substring(fileSize.length() - 2);
         if ("GB".equals(sizeType)) {
            fileSizeInMB *= 1024;
         }

         LOGGER.setLevel(Level.INFO);
         int agwLogSize = (fileSizeInMB - 10 * maxBackupIndex) / maxBackupIndex;
         LOGGER.activateAppenderWithSizeRolling("agw", "agw.log", "GBK", agwLogSize + "MB", maxBackupIndex - 1);
         LOGGER.setAdditivity(false);
         LOGGER_PERF.setLevel(Level.INFO);
         LOGGER_PERF.activateAppenderWithSizeRolling("agw", "agw-perf.log", "GBK", "32MB", maxBackupIndex - 1);
         LOGGER_PERF.setAdditivity(false);
         System.setProperty("AGW.LOG.PATH", LoggerHelper.getLogpath() + "agw");
         activateAsync();
      } finally {
         Thread.currentThread().setContextClassLoader(loader);
      }

   }

   private static void activateAsync() {
      LoggerUtil.activateAsync(LOGGER, 1, 0, false, 1000, false);
      LoggerUtil.activateAsync(LOGGER_PERF, 1, 0, false, 1000, false);
   }

   public static String changeLogLevel(int level) throws Exception {
      switch(level) {
      case 0:
         LOGGER.setLevel(Level.DEBUG);
         LOGGER_PERF.setLevel(Level.DEBUG);
         return Level.DEBUG.toString();
      case 1:
      default:
         LOGGER.setLevel(Level.INFO);
         LOGGER_PERF.setLevel(Level.INFO);
         return Level.INFO.toString();
      case 2:
         LOGGER.setLevel(Level.WARN);
         LOGGER_PERF.setLevel(Level.WARN);
         return Level.WARN.toString();
      case 3:
         LOGGER.setLevel(Level.ERROR);
         LOGGER_PERF.setLevel(Level.ERROR);
         return Level.ERROR.toString();
      case 4:
         LOGGER.setLevel(Level.OFF);
         LOGGER_PERF.setLevel(Level.OFF);
         return Level.OFF.toString();
      }
   }

   public static void changeLogLevel(String levelString) {
      try {
         levelString = changeFatalToOff(levelString);
         Level level = Level.codeOf(levelString.toUpperCase());
         LOGGER.setLevel(Level.WARN);
         LOGGER.warn("change log level to " + level.toString());
         LOGGER.setLevel(level);
         LOGGER_PERF.setLevel(level);
      } catch (Throwable var2) {
         LOGGER.error("", "change log level failed", var2);
      }

   }

   private static String changeFatalToOff(String levelString) {
      return "FATAL".equalsIgnoreCase(levelString) ? "OFF" : levelString;
   }

   public static void main(String[] args) {
      LOGGER.warn("123-warn");
      LOGGER.warn("123-warn");
      LOGGER.warn("123-warn");
      LOGGER.warn("123-warn");
      ORIGIN_LOGGER.warn("1234-warn");
      ORIGIN_LOGGER.warn("1234-warn");
      ORIGIN_LOGGER.warn("1234-warn");
   }

   static {
      LOGGER = new AppLogger(ORIGIN_LOGGER);
      LOGGER_PERF = new AppLogger(LoggerFactory.getLogger("taobao.agw.perf"));
      int maxBackupIndex = Integer.valueOf(System.getProperty("JM.LOG.RETAIN.COUNT", "2"));
      String fileSize = System.getProperty("JM.LOG.FILE.SIZE", "100MB");
      if (!fileSize.endsWith("MB") && !fileSize.endsWith("GB")) {
         throw new IllegalArgumentException("The value of JM.LOG.FILE.SIZE must end with MB or GB, such as 100MB");
      } else {
         initAGWLogWithSizeRolling(maxBackupIndex, fileSize);
      }
   }
}
