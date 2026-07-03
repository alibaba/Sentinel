/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.jaxrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Add the JVM parameter to connect to the dashboard:</p>
 * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-jax-rs}
 *
 * @author sea
 */
@SpringBootApplication
public class JaxRsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaxRsDemoApplication.class);
    }
}
