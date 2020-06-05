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
package com.alibaba.csp.sentinel.demo.okhttp.controller;

import com.alibaba.csp.sentinel.adapter.okhttp.SentinelOkHttpInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author zhaoyuguang
 */
@RestController
public class OkHttpTestController {

    @Value("${server.port}")
    private Integer port;

    @RequestMapping("/okhttp/back")
    public String back() {
        return "Welcome Back!";
    }

    @RequestMapping("/okhttp/back/{id}")
    public String back(@PathVariable String id) {
        return "Welcome Back! " + id;
    }

    @RequestMapping("/okhttp/testcase/{id}")
    public String testcase(@PathVariable String id) throws Exception {
        return getRemoteString(id);
    }

    @RequestMapping("/okhttp/testcase")
    public String testcase() throws Exception {
        return getRemoteString(null);
    }

    private String getRemoteString(String id) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor())
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/okhttp/back" + (id == null ? "" : "/" + id))
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}