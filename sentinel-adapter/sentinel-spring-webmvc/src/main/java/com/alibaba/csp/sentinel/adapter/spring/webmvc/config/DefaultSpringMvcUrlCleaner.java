package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-15
 */
public class DefaultSpringMvcUrlCleaner implements SpringMvcUrlCleaner {

  @Override
  public String clean(String originUrl) {
    return originUrl;
  }
}
