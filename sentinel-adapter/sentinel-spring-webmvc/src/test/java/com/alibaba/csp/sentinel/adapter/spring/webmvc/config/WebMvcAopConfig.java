package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.aop.SpringMvcSentinelAop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
//@Component
//@Aspect
public class WebMvcAopConfig {

  @Autowired
  private SpringMvcSentinelAop aop;

  @Around("execution(public * com.alibaba.csp.sentinel.adapter.spring.webmvc.controller..*(..))")
  public Object doAround(ProceedingJoinPoint point) throws Throwable {
    return aop.aroundAop(point);
  }

}
