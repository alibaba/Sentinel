package com.alibaba.csp.sentinel.adapter.spring.webmvc.callback;

/**
 * @author zhangkai
 */
public class DefaultUrlCleaner implements UrlCleaner {

  @Override
  public String clean(String originUrl) {
    return originUrl;
  }
}
