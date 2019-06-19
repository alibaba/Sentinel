package com.alibaba.csp.sentinel.log;

import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.util.Properties;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class LogConfigLocator {


    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";

    public static final String LOG_DIR = "csp.sentinel.log.dir";
    public static final String LOG_NAME_USE_PID = "csp.sentinel.log.use.pid";

    private static final String LOG_CONFIG = "csp.sentinel.config.file";
    private static final String DEFAULT_LOG_CONFIG = "sentinel.properties";


    private static final Properties properties = new Properties();

    static {
        locate();
    }

    private static void locate() {

        String fileName = System.getProperty(LOG_CONFIG);

        if (StringUtil.isNotBlank(fileName)) {
            //Load from JVM -D user-specified file
            properties.putAll(ConfigUtil.loadPropertiesFromFile(fileName));
        } else {
            //Load from default file
            properties.putAll(ConfigUtil.loadPropertiesFromFile(DEFAULT_LOG_CONFIG));

        }

        if (System.getProperties().contains(LOG_DIR)) {
            String oldValue = properties.getProperty(LOG_DIR);
            String newValue = System.getProperties().getProperty(LOG_DIR);
            properties.setProperty(LOG_DIR, newValue);
            System.out.println("[LogConfig] JVM parameter overrides: " + LOG_DIR + " " + oldValue + " -> " + newValue);
        }

        if (System.getProperties().contains(LOG_NAME_USE_PID)) {
            String oldValue = properties.getProperty(LOG_NAME_USE_PID);
            String newValue = System.getProperties().getProperty(LOG_NAME_USE_PID);
            properties.setProperty(LOG_NAME_USE_PID, System.getProperties().getProperty(LOG_DIR));
            System.out.println("[LogConfig] JVM parameter overrides: " + LOG_NAME_USE_PID + " " + oldValue + " -> " + newValue);

        }

    }


    /**
     * Locate the log print directory
     *
     * @return
     */
    public static String locateLogDir() {
        String logDir = properties.getProperty(LOG_DIR);
        if (StringUtil.isBlank(logDir)) {
            String userHome = System.getProperty(USER_HOME);
            logDir = addSeparator(userHome) + DIR_NAME + File.separator;
        }
        //make sure end with File.separator
        addSeparator(logDir);
        return logDir;
    }


    public static String locateLogUsePid() {
        return properties.getProperty(LOG_NAME_USE_PID);
    }

    private static String addSeparator(String logDir) {
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        return logDir;
    }
}
