/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.spring.sc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>A demo for Spring Cloud Gateway.</p>
 *
 * <p>To integrate with Sentinel dashboard, you can run the demo with the parameters (an example):
 * <code>-Dproject.name=spring-cloud-gateway -Dcsp.sentinel.dashboard.server=localhost:8080
 * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1
 * </code>
 * </p>
 *
 * @author Eric Zhao
 */
@SpringBootApplication
public class GatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }
}
