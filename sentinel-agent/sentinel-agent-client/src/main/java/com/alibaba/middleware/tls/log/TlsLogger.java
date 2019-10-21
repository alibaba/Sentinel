package com.alibaba.middleware.tls.log;
import com.alibaba.middleware.tls.util.TlsConstants;
import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;
public class TlsLogger {
	
	 private static Logger logger;

	    static {
	        logger = LoggerFactory.getLogger(TlsLogger.class);
	        String level = System.getProperty(TlsConstants.ENV_LOG_LEVEL, "INFO").toUpperCase();
	        logger.setLevel(Level.codeOf(level));
	        String size = System.getProperty(TlsConstants.ENV_LOG_SIZE, "256MB").toUpperCase();
	        if (!size.endsWith("KB") && !size.endsWith("MB") && !size.endsWith("GB")) {
	            if (size.endsWith("K") || size.endsWith("M") || size.endsWith("G")) {
	                size += "B";
	            }
	            else {
	                size += "MB";
	            }
	        }
	        String backupStr = System.getProperty(TlsConstants.ENV_LOG_BACKUP, "20");
	        int backup = Integer.valueOf(backupStr);
	        logger.activateAppenderWithSizeRolling("tls", "tls.log", "UTF-8", size, backup);
	        logger.setAdditivity(false);
	    }

	    public static void info(String s) {
	        logger.info(s);
	    }

	    public static void info(String appName, String s) {
	        logger.info(formatAppName(appName), s);
	    }

	    public static void warn(String s) {
	        logger.warn(s);
	    }

	    public static void warn(String appName, String s) {
	        logger.warn(formatAppName(appName), s);
	    }

	    public static void error(String s) {
	        logger.error("", s);
	    }

	    public static void error(String appName, String s) {
	        logger.error(formatAppName(appName), "", s);
	    }

	    public static void error(String s, Throwable throwable) {
	        logger.error("", s, throwable);
	    }

	    private static String formatAppName(String appName) {
	        return appName == null ? "" : appName;
	    }

}
