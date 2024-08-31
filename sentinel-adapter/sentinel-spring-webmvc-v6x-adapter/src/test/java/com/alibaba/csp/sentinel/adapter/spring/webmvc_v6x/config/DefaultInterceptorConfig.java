package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelExceptionAware;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.DefaultBlockExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Interceptor Config using DefaultBlockExceptionHandler
 *
 * @author Lingzhi
 */
@TestConfiguration
public class DefaultInterceptorConfig implements WebMvcConfigurer {

    @Bean
    public SentinelExceptionAware sentinelExceptionAware() {
        return new SentinelExceptionAware();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Add sentinel interceptor
        addSpringMvcInterceptor(registry);

        //If you want to sentinel the total flow, you can add total interceptor
        addSpringMvcTotalInterceptor(registry);
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();

        config.setBlockExceptionHandler(new DefaultBlockExceptionHandler());

        //Custom configuration if necessary
        config.setHttpMethodSpecify(false);
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Configure
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my_spring_mvc_total_url_request");

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");
    }
}
