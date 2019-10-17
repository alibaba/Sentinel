package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
@SpringBootApplication

//@EnableAspectJAutoProxy(exposeProxy = true)
//@ComponentScan
public class TestApplication {
  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class);
  }
}
