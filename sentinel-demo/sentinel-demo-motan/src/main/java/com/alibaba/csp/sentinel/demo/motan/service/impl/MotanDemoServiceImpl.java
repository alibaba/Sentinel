package com.alibaba.csp.sentinel.demo.motan.service.impl;

import com.alibaba.csp.sentinel.demo.motan.service.MotanDemoService;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zhangxn8
 **/
public class MotanDemoServiceImpl implements MotanDemoService {

    @Override
    public String hello(String name) {
        System.out.println(name);
        return "Hello " + name + "!";
    }
}
