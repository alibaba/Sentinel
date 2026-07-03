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

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * HelloResource
 * @author sea
 */
@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class HelloResource {

    @GET
    public HelloEntity sayHello() {
        return new HelloEntity("hello");
    }

    @GET
    @Path("/{id}")
    public HelloEntity get(@PathParam(value = "id") Long id) {
        return new HelloEntity(id, "hello");
    }

    @GET
    @Path("/list")
    public List<HelloEntity> getAll() {
        return IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> new HelloEntity((long)i, "hello"))
                .collect(Collectors.toList());
    }

    @Path("/ex")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String exception() {
        throw new RuntimeException("test exception mapper");
    }
}
