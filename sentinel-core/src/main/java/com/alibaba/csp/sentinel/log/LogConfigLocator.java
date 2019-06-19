package com.alibaba.csp.sentinel.log;

import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.util.Properties;

/**
 * <p>
 * class responsible for loading the Log configuration.
 * </p>
 *
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

        String newLogDir = System.getProperty(LOG_DIR);
        String oldLogDir = properties.getProperty(LOG_DIR);
        if (StringUtil.isNotBlank(newLogDir)) {
            properties.setProperty(LOG_DIR, newLogDir);
        }
        if (StringUtil.isNotBlank(oldLogDir) && StringUtil.isNotBlank(newLogDir)) {
            System.out.println("[LogConfig] JVM parameter overrides: " + LOG_DIR + " " + oldLogDir + " -> " + newLogDir);
        }

        String newLogUseId = System.getProperty(LOG_NAME_USE_PID);
        String oldLogUseId = properties.getProperty(LOG_NAME_USE_PID);
        if (StringUtil.isNotBlank(newLogUseId)) {
            properties.setProperty(LOG_NAME_USE_PID, newLogDir);
        }
        if (StringUtil.isNotBlank(oldLogUseId) && StringUtil.isNotBlank(newLogUseId)) {
            System.out.println("[LogConfig] JVM parameter overrides: " + LOG_NAME_USE_PID + " " + oldLogUseId + " -> " + newLogUseId);
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
