/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.okhttp.app.controller;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhaoyuguang
 */
@RestController
public class TestController {

    @RequestMapping("/okhttp/back")
    public String back() {
        return "Welcome Back!";
    }

    @RequestMapping("/okhttp/back/{id}")
    public String back(@PathVariable String id) {
        return "Welcome Back! " + id;
    }

    @RequestMapping("/okhttp/back/adaptive")
    public String back(HttpServletRequest request, HttpServletResponse response) {
        //This should be encapsulated by the downstream Sentinel. Here, only a simulation is conducted.
        String adaptiveHeader = request.getHeader("X-Sentinel-Adaptive");
        if ("enabled".equals(adaptiveHeader)) {
            response.setHeader("X-Server-Metrics", AdaptiveUtils.packServerMetric());
            return "Adaptive enabled received";
        }
        return "Adaptive enabled unreceived ";
    }
}