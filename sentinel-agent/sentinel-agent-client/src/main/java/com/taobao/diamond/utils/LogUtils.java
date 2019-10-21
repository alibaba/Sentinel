package com.taobao.diamond.utils;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;

/**
 * Created by dingjoey on 14-6-30.
 */
public class LogUtils {

    static {
        // logger init
        Logger logger = LoggerFactory.getLogger("com.taobao.diamond.client");
        logger.setLevel(Level.INFO);
        logger.setAdditivity(false);
        logger.activateAppenderWithTimeAndSizeRolling("diamond-client", "diamond-client.log", "GBK", "10MB"); //TODO
    }

   public static Logger logger(Class<?> clazz) {
       return LoggerFactory.getLogger(clazz);
   }
}
