package com.alibaba.csp.ahas.sentinel.adapter.servlet;

import java.util.Map;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;

/**
 * @author Eric Zhao
 */
public class PropertyUrlCleaner implements UrlCleaner {

    public static final String SUFFIX_CLEANED_URL = "suffix_cleaned_url";

    @Override
    public String clean(String originUrl) {
        String[] suffixPattern = UrlCleanerPropertyLocalConfig.getSuffixExcludeMap();
        if (suffixPattern != null) {
            for (String suffix : suffixPattern) {
                if (originUrl.endsWith(suffix)) {
                    return SUFFIX_CLEANED_URL;
                }
            }
        }
        if (UrlCleanerPropertyLocalConfig.getConfigMap().isEmpty()) {
            return originUrl;
        }
        for (Map.Entry<String, String> e : UrlCleanerPropertyLocalConfig.getConfigMap().entrySet()) {
            if (originUrl.startsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return originUrl;
    }
}
