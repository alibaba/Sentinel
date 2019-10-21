package com.alibaba.csp.ahas.sentinel.adapter.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alibaba.csp.ahas.sentinel.util.FileConfigUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public final class UrlCleanerPropertyLocalConfig {

    public static final String URL_CONFIG_FILE_KEY = "csp.sentinel.url.clean.config.path";
    public static final String STATIC_SUFFIX_EXCLUDE_KEY = "csp.sentinel.url.suffix.exclude.pattern";

    public static final String DEFAULT_URL_CONFIG_FILE_NAME = "ahas-sentinel-url-clean.properties";
    public static final String DEFAULT_EXCLUDE_SUFFIX_PATTERN = "png,gif,js,css,htm,html,jpg,jpeg,map,ico,ttf,woff";

    private static final Map<String, String> CONFIG_MAP = new HashMap<String, String>();
    private static String[] SUFFIX_EXCLUDE_MAP = null;

    static {
        try {
            loadConfigFromFile();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void loadConfigFromFile() {
        String excludeSuffixPattern = System.getProperty(STATIC_SUFFIX_EXCLUDE_KEY);
        if (StringUtil.isBlank(excludeSuffixPattern)) {
            excludeSuffixPattern = DEFAULT_EXCLUDE_SUFFIX_PATTERN;
        }
        String[] arr = excludeSuffixPattern.split(",");
        if (arr.length > 0) {
            SUFFIX_EXCLUDE_MAP = arr;
        }

        String filePath = System.getProperty(URL_CONFIG_FILE_KEY);
        if (StringUtil.isEmpty(filePath)) {
            filePath = DEFAULT_URL_CONFIG_FILE_NAME;
        }
        Properties properties = FileConfigUtil.loadProperties(filePath);
        if (properties != null && !properties.isEmpty()) {
            RecordLog.info("[UrlCleanerPropertyLocalConfig] Loading UrlCleaner config from " + filePath);
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                RecordLog.info("[UrlCleanerPropertyLocalConfig] URL with prefix <{0}> will be unified as <{1}>",
                    e.getKey(), e.getValue());
                CONFIG_MAP.put(e.getKey().toString(), e.getValue().toString());
            }
        }
    }

    static String[] getSuffixExcludeMap() {
        return SUFFIX_EXCLUDE_MAP;
    }

    public static Map<String, String> getConfigMap() {
        return CONFIG_MAP;
    }

    private UrlCleanerPropertyLocalConfig() {}
}
