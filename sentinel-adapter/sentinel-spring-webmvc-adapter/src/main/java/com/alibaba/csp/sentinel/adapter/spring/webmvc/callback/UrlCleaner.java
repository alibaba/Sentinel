package com.alibaba.csp.sentinel.adapter.spring.webmvc.callback;

/**
 * Clean sentinel target
 * @author zhangkai
 */
public interface UrlCleaner {

    /**
     * Clean sentinel target
     * @param originUrl
     * @return
     */
    String clean(String originUrl);
}
