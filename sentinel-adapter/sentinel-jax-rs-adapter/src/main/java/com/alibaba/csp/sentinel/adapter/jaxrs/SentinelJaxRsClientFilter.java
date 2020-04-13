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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.jaxrs.config.SentinelJaxRsConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * @author sea
 */
public class SentinelJaxRsClientFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final String SENTINEL_JAX_RS_CLIENT_ENTRY_PROPERTY = "sentinel_jax_rs_client_entry_property";

    @Context
    private ResourceInfo resourceInfo;


    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        try {
            String resourceName = getResourceName(requestContext);

            if (StringUtil.isNotEmpty(resourceName)) {
                Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);

                requestContext.setProperty(SENTINEL_JAX_RS_CLIENT_ENTRY_PROPERTY, entry);
            }
        } catch (BlockException e) {
            try {
                requestContext.abortWith(SentinelJaxRsConfig.getJaxRsFallback().fallbackResponse(requestContext.getUri().toString(), e));
            } finally {
                ContextUtil.exit();
            }
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Entry entry = (Entry) requestContext.getProperty(SENTINEL_JAX_RS_CLIENT_ENTRY_PROPERTY);
        if (entry != null) {
            entry.exit();
        }
        requestContext.removeProperty(SENTINEL_JAX_RS_CLIENT_ENTRY_PROPERTY);
    }

    public String getResourceName(ClientRequestContext requestContext) {
        return SentinelJaxRsConfig.getResourceNameParser().parse(requestContext);
    }
}
