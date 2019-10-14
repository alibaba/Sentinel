package com.alibaba.csp.sentinel.demo.spring.webmvc;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author taodizhou
 * @description
 * @date 2019-10-14
 * <code>
 *   -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-spring-webmvc
 * </code>
 */
@SpringBootApplication
public class WebMvcDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebMvcDemoApplication.class);
  }
}
