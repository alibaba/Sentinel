package com.alibaba.csp.sentinel.demo.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.interceptor.SentinelSpringWebMvcInterceptor;
import com.alibaba.csp.sentinel.demo.spring.webmvc.handler.MyUrlBlockHandler;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhangkai
 * @description
 * @date 2019-09-25
 */
@Configuration
public class WebMvcSentinelConfig implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry){
    registry.addInterceptor(new SentinelSpringWebMvcInterceptor()).addPathPatterns("/**");
  }

  @PostConstruct
  public void blockHandler() {
    WebCallbackManager.setUrlBlockHandler(new MyUrlBlockHandler());
  }
}
