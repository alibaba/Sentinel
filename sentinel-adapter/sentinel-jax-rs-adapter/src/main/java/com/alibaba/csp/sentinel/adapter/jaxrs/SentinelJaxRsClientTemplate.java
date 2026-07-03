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
package com.alibaba.csp.sentinel.adapter.jaxrs;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.jaxrs.config.SentinelJaxRsConfig;
import com.alibaba.csp.sentinel.adapter.jaxrs.future.FutureWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;

import javax.ws.rs.core.Response;
import java.util.concurrent.Future;


/**
 * wrap jax-rs client execution with sentinel
 * <pre>
 *         Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
 *
 *             @Override
 *             public Response get() {
 *                 return client.target(host).path(url).request()
 *                         .get();
 *             }
 *         });
 * </pre>
 * @author sea
 */
public class SentinelJaxRsClientTemplate {

    /**
     * execute supplier with sentinel
     * @param resourceName
     * @param supplier
     * @return
     */
    public static Response execute(String resourceName, Supplier<Response> supplier) {
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return supplier.get();
        } catch (BlockException ex) {
            return SentinelJaxRsConfig.getJaxRsFallback().fallbackResponse(resourceName, ex);
        } finally {
            System.out.println("entry exit");
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * execute supplier with sentinel
     * @param resourceName
     * @param supplier
     * @return
     */
    public static Future<Response> executeAsync(String resourceName, Supplier<Future<Response>> supplier) {
        try {
            AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return new FutureWrapper<>(entry, supplier.get());
        } catch (BlockException ex) {
            return SentinelJaxRsConfig.getJaxRsFallback().fallbackFutureResponse(resourceName, ex);
        }
    }
}
