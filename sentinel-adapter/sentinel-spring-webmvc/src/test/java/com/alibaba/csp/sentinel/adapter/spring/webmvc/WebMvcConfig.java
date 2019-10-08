package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.interceptor.SentinelSpringWebMvcInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  //配置拦截器
  @Override
  public void addInterceptors(InterceptorRegistry registry){
    registry.addInterceptor(new SentinelSpringWebMvcInterceptor()).addPathPatterns("/**");
  }
}
