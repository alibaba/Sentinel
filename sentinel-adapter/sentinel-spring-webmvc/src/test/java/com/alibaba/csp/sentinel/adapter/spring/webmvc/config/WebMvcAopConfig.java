package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.aop.SpringMvcAop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
@Component
@Aspect
public class WebMvcAopConfig {

  private SpringMvcAop aop = new SpringMvcAop();

  @Around("execution(public * com.alibaba.csp.sentinel.adapter.spring.webmvc.controller..*(..))")
  public Object doAround(ProceedingJoinPoint point) throws Throwable {
    return aop.aroundAop(point);
  }
}
