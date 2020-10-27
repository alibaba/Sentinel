package com.alibaba.csp.sentinel.demo.motan.service.impl;

import com.alibaba.csp.sentinel.demo.motan.service.MotanDemoService;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zxn
 * create: 2020-10-28 01:00
 **/
public class MotanDemoServiceImpl implements MotanDemoService {

    @Override
    public String hello(String name) {
        System.out.println(name);
        return "Hello " + name + "!";
    }
}
