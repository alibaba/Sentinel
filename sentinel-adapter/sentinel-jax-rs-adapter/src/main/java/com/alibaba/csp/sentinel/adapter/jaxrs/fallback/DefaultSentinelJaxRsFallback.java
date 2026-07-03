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
package com.alibaba.csp.sentinel.adapter.jaxrs.fallback;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author sea
 */
public class DefaultSentinelJaxRsFallback implements SentinelJaxRsFallback {
    @Override
    public Response fallbackResponse(String route, Throwable cause) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity("Blocked by Sentinel (flow limiting)")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @Override
    public Future<Response> fallbackFutureResponse(final String route, final Throwable cause) {
        return new FutureTask<>(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return fallbackResponse(route, cause);
            }
        });
    }
}
