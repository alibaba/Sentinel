/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelSpringMvcTotalConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring mvc interceptor for all requests.
 *
 * @Author kaizi2009
 */
public class SentinelTotalInterceptor extends AbstractSentinelInterceptor {
    private SentinelSpringMvcTotalConfig config;

    public SentinelTotalInterceptor(SentinelSpringMvcTotalConfig config) {
        if (config == null) {
            throw new SentinelSpringMvcException("config can not be null");
        }
        this.config = config;
    }

    public SentinelTotalInterceptor() {
        this(new SentinelSpringMvcTotalConfig());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            ContextUtil.enter(SPRING_MVC_CONTEXT_NAME);
            Entry entry = SphU.entry(config.getTotalTarget());
            final EntryContainer entryContainer = new EntryContainer().setUrlEntry(entry);
            setEntryContainerInReqeust(request, config.getRequestAttributeName(), entryContainer);
            return true;
        } catch (BlockException e) {
            //Throw BlockException and handle it in spring MVC
            throw e;
        } catch (RuntimeException e2) {
            Tracer.trace(e2);
            throw e2;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        EntryContainer entryContainer = getEntryContainerInReqeust(request, config.getRequestAttributeName());
        if (entryContainer != null) {
            if (entryContainer.getUrlEntry() != null) {
                entryContainer.getUrlEntry().exit();
            }
            removeEntryContainerInReqeust(request, config.getRequestAttributeName());
        }
        ContextUtil.exit();
    }

}
