package com.alibaba.csp.sentinel.adapter.spring.webmvc.callback;

/**
 * @author zhangkai
 */
public interface UrlCleaner {

    String clean(String originUrl);
}
