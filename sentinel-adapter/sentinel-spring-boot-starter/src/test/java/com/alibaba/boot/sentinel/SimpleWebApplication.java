/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.boot.sentinel;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Eric Zhao
 */
@SpringBootApplication
@RestController
@PropertySource("classpath:web-servlet.properties")
public class SimpleWebApplication {

    @RequestMapping("/foo")
    public String foo() {
        return "Hello!";
    }

    @RequestMapping("/baz")
    public String baz() {
        ClusterNode node = ClusterBuilderSlot.getClusterNode("/foo");
        if (node == null) {
            return "/foo has not been called!";
        } else {
            return "/foo total request in metrics: " + node.totalRequest();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SimpleWebApplication.class, args);
    }
}
