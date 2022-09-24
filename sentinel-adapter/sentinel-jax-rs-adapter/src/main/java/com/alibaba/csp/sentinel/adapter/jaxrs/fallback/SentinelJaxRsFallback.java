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

import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

/**
 * @author sea
 */
public interface SentinelJaxRsFallback {

    /**
     * Provides a fallback response based on the cause of the failed execution.
     *
     * @param route The route the fallback is for
     * @param cause cause of the main method failure, may be <code>null</code>
     * @return the fallback response
     */
    Response fallbackResponse(String route, Throwable cause);

    /**
     * Provides a fallback response future based on the cause of the failed execution.
     *
     * @param route The route the fallback is for
     * @param cause cause of the main method failure, may be <code>null</code>
     * @return the fallback response future
     */
    Future<Response> fallbackFutureResponse(String route, Throwable cause);
}
