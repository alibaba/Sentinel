package com.taobao.diamond.client.impl;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;

/**
 * Created by dingjoey on 14-6-30.
 */
public class LogUtils {

	static int JM_LOG_RETAIN_COUNT = 7;
	static String JM_LOG_FILE_SIZE = "10MB";

    static {
    	String tmp = "7";
        try {
            tmp = System.getProperty("JM.LOG.RETAIN.COUNT","7"); //change timeout from 100 to 200
            JM_LOG_RETAIN_COUNT = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            log.error("invalid value settings for JM.LOG.RETAIN.COUNT", "DIAMOND-XXXX", e);
        }
        
        JM_LOG_FILE_SIZE = System.getProperty("JM.LOG.FILE.SIZE","10MB"); 
    	
        // logger init
        Logger logger = LoggerFactory.getLogger("com.taobao.diamond.client");
        logger.setLevel(Level.INFO);
        logger.setAdditivity(false);
        logger.activateAppenderWithSizeRolling("diamond-client", "diamond-client.log", "GBK", JM_LOG_FILE_SIZE, JM_LOG_RETAIN_COUNT);
    }

   public static Logger logger(Class<?> clazz) {
       return LoggerFactory.getLogger(clazz);
   }
}
