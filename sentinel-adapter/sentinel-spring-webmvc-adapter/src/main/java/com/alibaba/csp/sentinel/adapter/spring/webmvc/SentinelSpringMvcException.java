package com.alibaba.csp.sentinel.adapter.spring.webmvc;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-17
 */
public class SentinelSpringMvcException extends RuntimeException {
  public SentinelSpringMvcException(String errorMsg) {
    super(errorMsg);
  }
}
