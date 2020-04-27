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
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author sea
 */
@Provider
public class SentinelJaxRsProviderFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String SENTINEL_JAX_RS_PROVIDER_CONTEXT_NAME = "sentinel_jax_rs_provider_context";


    private static final String SENTINEL_JAX_RS_PROVIDER_ENTRY_PROPERTY = "sentinel_jax_rs_provider_entry_property";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        try {
            String resourceName = getResourceName(containerRequestContext, resourceInfo);

            if (StringUtil.isNotEmpty(resourceName)) {
                // Parse the request origin using registered origin parser.
                String origin = parseOrigin(containerRequestContext);
                String contextName = getContextName(containerRequestContext);
                ContextUtil.enter(contextName, origin);
                Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN);

                containerRequestContext.setProperty(SENTINEL_JAX_RS_PROVIDER_ENTRY_PROPERTY, entry);
            }
        } catch (BlockException e) {
            try {
                containerRequestContext.abortWith(SentinelJaxRsConfig.getJaxRsFallback().fallbackResponse(containerRequestContext.getUriInfo().getPath(), e));
            } finally {
                ContextUtil.exit();
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Entry entry = (Entry) containerRequestContext.getProperty(SENTINEL_JAX_RS_PROVIDER_ENTRY_PROPERTY);
        if (entry != null) {
            entry.exit();
        }
        containerRequestContext.removeProperty(SENTINEL_JAX_RS_PROVIDER_ENTRY_PROPERTY);
        ContextUtil.exit();
    }

    public String getResourceName(ContainerRequestContext containerRequestContext, ResourceInfo resourceInfo) {
        return SentinelJaxRsConfig.getResourceNameParser().parse(containerRequestContext, resourceInfo);
    }

    protected String getContextName(ContainerRequestContext request) {
        return SENTINEL_JAX_RS_PROVIDER_CONTEXT_NAME;
    }

    protected String parseOrigin(ContainerRequestContext request) {
        return SentinelJaxRsConfig.getRequestOriginParser().parseOrigin(request);
    }
}
