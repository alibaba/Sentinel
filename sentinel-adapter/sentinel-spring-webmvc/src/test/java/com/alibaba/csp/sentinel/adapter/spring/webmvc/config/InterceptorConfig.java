package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.interceptor.SentinelSpringWebMvcInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-17
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry){
    //Config
    SpringMvcSentinelAopConfig config = new SpringMvcSentinelAopConfig()
        .setContextName("my_context_name")
        .setHttpMethodSpecify(false)
        .setOriginParser(request -> request.getHeader("S-user"));
    SentinelSpringWebMvcInterceptor sentinelSpringWebMvcInterceptor = new SentinelSpringWebMvcInterceptor(
        config);
    //Add sentinel interceptor
    registry.addInterceptor(sentinelSpringWebMvcInterceptor)
        .addPathPatterns("/**");
  }
}
