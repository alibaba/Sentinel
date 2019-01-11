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

import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.zuul.exception.ZuulException;

import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.POST_TYPE;

/**
 * This filter do success routing RT statistic and exit {@link com.alibaba.csp.sentinel.Entry}
 *
 * @author tiger
 */
public class SentinelPostFilter extends AbstractSentinelFilter {

    public SentinelPostFilter(SentinelZuulProperties sentinelZuulProperties) {
        super(sentinelZuulProperties);
    }

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getPost();
    }

    /**
     * While loop will exit all entries,
     * Case serviceId and URL entry twice in {@link SentinelPreFilter}.
     * The ContextUtil.getContext().getCurEntry() will exit from inner to outer.
     */
    @Override
    public Object run() throws ZuulException {
        while (ContextUtil.getContext() != null && ContextUtil.getContext().getCurEntry() != null) {
            ContextUtil.getContext().getCurEntry().exit();
        }
        ContextUtil.exit();
        return null;
    }
}
