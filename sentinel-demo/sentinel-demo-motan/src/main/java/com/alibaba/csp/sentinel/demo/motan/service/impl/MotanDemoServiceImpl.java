package com.alibaba.csp.sentinel.demo.motan.service.impl;

import com.alibaba.csp.sentinel.demo.motan.service.MotanDemoService;

/**
 * author: zhangxn8
 */
public class MotanDemoServiceImpl implements MotanDemoService {

    @Override
    public String hello(String name) {
        System.out.println(name);
        return "Hello " + name + "!";
    }
}
