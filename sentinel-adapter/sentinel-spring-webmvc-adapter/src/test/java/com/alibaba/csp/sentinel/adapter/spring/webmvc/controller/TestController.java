package com.alibaba.csp.sentinel.adapter.spring.webmvc.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangkai
 */
@RestController
public class TestController {

  @GetMapping("/hello")
  public String apiHello() {
    doBusiness();
    return "Hello!";
  }

  @GetMapping("/err")
  public String apiError() {
    doBusiness();
    return "Oops...";
  }

  @GetMapping("/foo/{id}")
  public String apiFoo(@PathVariable("id") Long id) {
    doBusiness();
    return "foo " + id;
  }

  @GetMapping("/exclude/{id}")
  public String apiExclude(@PathVariable("id") Long id) {
    doBusiness();
    return "Exclude " + id;
  }

  private void doBusiness() {
    Random random = new Random(1);
    try {
      TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
