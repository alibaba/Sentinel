package com.alibaba.csp.sentinel.demo.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelSpringMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelSpringMvcTotalConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Config sentinel interceptor
 * @Author kaizi2009
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Add sentinel interceptor
        addSpringMvcInterceptor(registry);

        //If you want to sentinel the total flow, you can add total interceptor
        addSpringMvcTotalInterceptor(registry);
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelSpringMvcConfig config = new SentinelSpringMvcConfig();

        //Custom configuration if necessary
        config.setHttpMethodSpecify(false)
                .setOriginParser(request -> request.getHeader("S-user"));

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelInterceptor(config)).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelSpringMvcTotalConfig config = new SentinelSpringMvcTotalConfig();

        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container")
                .setTotalTarget("my_spring_mvc_total_url_request");

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelTotalInterceptor(config)).addPathPatterns("/**");
    }
}
