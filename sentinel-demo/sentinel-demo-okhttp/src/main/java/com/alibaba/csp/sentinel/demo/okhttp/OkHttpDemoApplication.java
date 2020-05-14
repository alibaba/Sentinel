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
package com.alibaba.csp.sentinel.demo.okhttp;

import com.alibaba.csp.sentinel.adapter.okhttp.config.SentinelOkHttpConfig;
import com.alibaba.csp.sentinel.adapter.okhttp.extractor.OkHttpResourceExtractor;
import okhttp3.Connection;
import okhttp3.Request;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhaoyuguang
 */
@SpringBootApplication
public class OkHttpDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OkHttpDemoApplication.class);
    }

    @Override
    public void run(String... args) {
        SentinelOkHttpConfig.setExtractor(new OkHttpResourceExtractor() {
            @Override
            public String extract(String url, Request request, Connection connection) {
                String regex = "/okhttp/back/";
                if (url.contains(regex)) {
                    url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
                }
                return request.method() + ":" + url;
            }
        });
    }
}