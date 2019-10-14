package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
@SpringBootApplication

//@EnableAspectJAutoProxy(exposeProxy = true)
//@ComponentScan
public class TestApp {
  public static void main(String[] args) {
    SpringApplication.run(TestApp.class);
  }
}
