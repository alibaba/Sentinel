package com.alibaba.csp.sentinel.demo.spring.webmvc;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
@SpringBootApplication
public class WebMvcDemoApplication {

  public static void main(String[] args) {
    System.setProperty(TransportConfig.CONSOLE_SERVER, "127.0.0.1:8080");
    System.setProperty("project.name", "sentinel-demo-spring-webmvc");
    SpringApplication.run(WebMvcDemoApplication.class);
  }
}
