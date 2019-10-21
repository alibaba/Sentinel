package com.taobao.csp.ahas.module.util;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;
import com.taobao.middleware.logger.support.LoggerHelper;

public class LoggerInit {
   public static final Logger LOGGER = new AppLogger(LoggerFactory.getLogger("ahas-java-agent"));

   private static void initAGWLogWithSizeRolling(int maxBackupIndex, String fileSize) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      try {
         Thread.currentThread().setContextClassLoader(LoggerInit.class.getClassLoader());
         int fileSizeInMB = Integer.valueOf(fileSize.substring(0, fileSize.length() - 2));
         String sizeType = fileSize.substring(fileSize.length() - 2);
         if ("GB".equals(sizeType)) {
            fileSizeInMB *= 1024;
         }

         changeLogLevel(System.getProperty("ahas.log.level", "INFO"));
         int agwLogSize = (fileSizeInMB - 10 * maxBackupIndex) / maxBackupIndex;
         LOGGER.activateAppenderWithSizeRolling("ahas", "ahas-agent.log", "GBK", agwLogSize + "MB", maxBackupIndex - 1);
         LOGGER.setAdditivity(false);
         System.setProperty("AHAS.LOG.PATH", LoggerHelper.getLogpath() + "ahas");
         activateAsync();
      } finally {
         Thread.currentThread().setContextClassLoader(loader);
      }

   }

   private static void activateAsync() {
      LoggerUtil.activateAsync(LOGGER, 1, 0, false, 1000, false);
   }

   public static String changeLogLevel(int level) throws Exception {
      switch(level) {
      case 0:
         LOGGER.setLevel(Level.DEBUG);
         return Level.DEBUG.toString();
      case 1:
      default:
         LOGGER.setLevel(Level.INFO);
         return Level.INFO.toString();
      case 2:
         LOGGER.setLevel(Level.WARN);
         return Level.WARN.toString();
      case 3:
         LOGGER.setLevel(Level.ERROR);
         return Level.ERROR.toString();
      case 4:
         LOGGER.setLevel(Level.OFF);
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
      } catch (Throwable var2) {
         LOGGER.error("", "change log level failed", var2);
      }

   }

   private static String changeFatalToOff(String levelString) {
      return "FATAL".equalsIgnoreCase(levelString) ? "OFF" : levelString;
   }

   static {
      int maxBackupIndex = Integer.valueOf(System.getProperty("JM.LOG.RETAIN.COUNT", "2"));
      String fileSize = System.getProperty("JM.LOG.FILE.SIZE", "40MB");
      if (!fileSize.endsWith("MB") && !fileSize.endsWith("GB")) {
         throw new IllegalArgumentException("The value of JM.LOG.FILE.SIZE must end with MB or GB, such as 100MB");
      } else {
         initAGWLogWithSizeRolling(maxBackupIndex, fileSize);
      }
   }
}
