package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangkai
 * @description
 * @date 2019-09-25
 */
@RestController
@RequestMapping("test")
public class TestController {

  @GetMapping("/foo/{id}")
  public String apiFoo(@PathVariable("id") Long id) {
    return "Hello " + id;
  }
}
