package com.alibaba.csp.sentinel.demo.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.aop.SpringMvcSentinelAop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author zhangkai
 * @description
 * @date 2019-09-25
 */
@Component
@Aspect
public class WebMvcAopConfig {

  private SpringMvcSentinelAop aop = new SpringMvcSentinelAop();

  @Around("execution(public * com.alibaba.csp.sentinel.demo..*.controller..*(..))")
  public Object doAround(ProceedingJoinPoint point) throws Throwable {
    return aop.aroundAop(point);
  }
}
