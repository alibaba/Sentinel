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

package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.*;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.adapter.zuul.util.FilterUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;

import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.PRE_TYPE;
import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.SERVICE_ID_KEY;
import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.ZUUL_DEFAULT_CONTEXT;

/**
 * This pre filter get an entry of resource,the first order is ServiceId, then API Path.
 * When get a BlockException run fallback logic.
 *
 * @author tiger
 */
public class SentinelPreFilter extends AbstractSentinelFilter {

    private final UrlCleaner urlCleaner;

    private final RequestOriginParser requestOriginParser;

    public SentinelPreFilter(SentinelZuulProperties sentinelZuulProperties,
                             UrlCleaner urlCleaner,
                             RequestOriginParser requestOriginParser) {
        super(sentinelZuulProperties);
        AssertUtil.notNull(urlCleaner, "UrlCleaner can not be null");
        AssertUtil.notNull(requestOriginParser, "RequestOriginParser can not be null");
        this.urlCleaner = urlCleaner;
        this.requestOriginParser = requestOriginParser;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * This run before route filter so we can get more accurate RT time.
     */
    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getPre();
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String origin = parseOrigin(ctx.getRequest());
        String serviceTarget = (String) ctx.get(SERVICE_ID_KEY);
        // When serviceId blocked first get the service level fallback provider.
        String fallBackRoute = serviceTarget;
        try {
            if (StringUtil.isNotEmpty(serviceTarget)) {
                RecordLog.info(String.format("[Sentinel Pre Filter] Origin: %s enter ServiceId: %s", origin, serviceTarget));
                ContextUtil.enter(serviceTarget, origin);
                SphU.entry(serviceTarget, EntryType.IN);
            } else {
                RecordLog.info("[Sentinel Pre Filter] ServiceId is empty");
                ContextUtil.enter(ZUUL_DEFAULT_CONTEXT, origin);
            }
            String uriTarget = FilterUtil.filterTarget(ctx.getRequest());
            // Clean and unify the URL.
            // For REST APIs, you have to clean the URL (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`), or
            // the amount of context and resources will exceed the threshold.
            uriTarget = urlCleaner.clean(uriTarget);
            fallBackRoute = uriTarget;
            RecordLog.info(String.format("[Sentinel Pre Filter] Origin: %s enter Uri Path: %s", origin, uriTarget));
            SphU.entry(uriTarget, EntryType.IN);
        } catch (BlockException ex) {
            RecordLog.warn(String.format("[Sentinel Pre Filter] Block Exception when Origin: %s enter fall back route: %s", origin, fallBackRoute), ex);
            ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(fallBackRoute);
            BlockResponse blockResponse = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, ex);
            // prevent routing from running
            ctx.setRouteHost(null);
            ctx.set(SERVICE_ID_KEY, null);
            ctx.setResponseBody(blockResponse.toString());
        }
        return null;
    }

    private String parseOrigin(HttpServletRequest request) {
        return requestOriginParser.parseOrigin(request);
    }
}
