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

import com.alibaba.csp.sentinel.adapter.jaxrs.SentinelJaxRsClientTemplate;
import com.alibaba.csp.sentinel.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

/**
 * @author sea
 */
public class JaxRsClientDemo {

    public static void main(String[] args) {
        Client client = ClientBuilder.newBuilder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        final String host = "http://127.0.0.1:8181";
        final String url = "/hello/1";
        String resourceName = "GET:" + url;
        Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {

            @Override
            public Response get() {
                return client.target(host).path(url).request()
                        .get();
            }
        });
        System.out.println(response.readEntity(HelloEntity.class));
        System.exit(0);
    }
}
