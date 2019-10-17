package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.aop.SpringMvcSentinelAop;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-15
 */
@Configuration
public class BeanConfig {
  @Bean
  public SpringMvcSentinelAop newSpringMvcSentinelAop() {
    SpringMvcSentinelAopConfig config = new SpringMvcSentinelAopConfig()
        .setContextName("my_context_name")
        .setHttpMethodSpecify(false)
        .setOriginParser(request -> request.getHeader("S-user"));
    return new SpringMvcSentinelAop(config);
  }

}
