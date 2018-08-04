package com.alibaba.csp.sentinel.transport.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("test")
public class TestController {

	
	@RequestMapping("hello")
	public void hello() {
		System.out.println("hello world");
	}
}
