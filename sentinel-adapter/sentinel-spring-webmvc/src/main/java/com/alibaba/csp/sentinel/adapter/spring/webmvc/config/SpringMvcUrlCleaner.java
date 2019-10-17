package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-15
 */
public interface SpringMvcUrlCleaner {

  String clean(String originUrl);
}
