package com.alibaba.csp.ahas.sentinel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

public final class FileConfigUtil {

    public static final String CLASSPATH_FILE_FLAG = "classpath:";

    /**
     * <p>Load the properties from provided file.</p>
     * <p>Currently it supports reading from classpath file or local file.</p>
     *
     * @param fileName valid file path
     * @return the retrieved properties from the file; null if the file not exist
     */
    public static Properties loadProperties(String fileName) {
        if (StringUtil.isNotBlank(fileName)) {
            if (fileName.startsWith(CLASSPATH_FILE_FLAG)) {
                return loadPropertiesFromClasspathFile(fileName);
            } else {
                return loadPropertiesFromAbsoluteFile(fileName);
            }
        } else {
            return null;
        }
    }

    private static Properties loadPropertiesFromAbsoluteFile(String fileName) {
        Properties properties = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }
            try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileName),
                Charset.forName(SentinelConfig.charset()))) {
                properties = new Properties();
                properties.load(bufferedReader);
            }
        } catch (Throwable e) {
            RecordLog.warn("Error when loading properties from: {0}", fileName, e);
            e.printStackTrace();
        }
        return properties;
    }

    private static boolean absolutePathStart(String path) {
        File[] files = File.listRoots();
        for (File file : files) {
            if (path.startsWith(file.getPath())) {
                return true;
            }
        }
        return false;
    }

    private static Properties loadPropertiesFromClasspathFile(String fileName) {
        fileName = fileName.substring(CLASSPATH_FILE_FLAG.length()).trim();

        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(fileName);
            list = new ArrayList<URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        for (URL url : list) {
            Properties p = new Properties();
            try (InputStreamReader reader = new InputStreamReader(url.openStream(),
                Charset.forName(SentinelConfig.charset()).newDecoder())) {
                p.load(reader);
                properties.putAll(p);
            } catch (Throwable e) {
                RecordLog.warn("Error when loading properties from: {0}", url.toString(), e);
                e.printStackTrace();
            }
        }
        return properties;
    }

    private static Properties loadPropertiesFromRelativeFile(String fileName) {
        String userDir = System.getProperty("user.dir");
        String realFilePath = addSeparator(userDir) + fileName;
        return loadPropertiesFromAbsoluteFile(realFilePath);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = FileConfigUtil.class.getClassLoader();
        }
        return classLoader;
    }

    public static String addSeparator(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    private FileConfigUtil() {}
}
