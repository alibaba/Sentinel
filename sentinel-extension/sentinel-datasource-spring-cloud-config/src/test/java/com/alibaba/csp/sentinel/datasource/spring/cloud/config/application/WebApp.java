package com.alibaba.csp.sentinel.datasource.spring.cloud.config.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*
import org.springframework.cloud.client.SpringCloudApplication;
*/

/**
 * @author lianglin
 * @since 1.7.0
 */

@SpringBootApplication
public class WebApp {
    public static void main(String[] args){
        SpringApplication.run(WebApp.class, args);

    }
}
