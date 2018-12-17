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
package com.alibaba.csp.sentinel.adapter.servlet;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.Context;

/**
 * AsyncListener wrapper
 * 
 * @author jason
 *
 */
public class CommonAsyncListener implements AsyncListener {
    private static CommonAsyncListener instance = null;

    private CommonAsyncListener() {
    }

    public static CommonAsyncListener getInstance() {
        if (instance == null) {
            synchronized (AsyncListener.class) {
                if (instance == null) {
                    instance = new CommonAsyncListener();
                }
            }
        }
        return instance;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        // all async request will call complete finally before reenter all filters including when timeout or errored
        HttpServletRequest request = (HttpServletRequest)event.getSuppliedRequest();
        if (request == null) {
            // no request, it's a out of control async servlet
            return;
        }
        Entry entry = (Entry) request.getAttribute(CommonFilter.ATTR_ENTRY);
        Entry methodEntry = (Entry) request.getAttribute(CommonFilter.ATTR_METHOD_ENTRY);
        if (methodEntry != null) {
            methodEntry.exit();
        }
        if (entry != null) {
            entry.exit();
        }
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        HttpServletRequest request = (HttpServletRequest)event.getSuppliedRequest();
        if (request == null) {
            return;
        }
        Context ctx = (Context)request.getAttribute(CommonFilter.ATTR_CONTEXT);
        if (ctx != null) {
            Tracer.traceContext(new TimeoutException(), 1, ctx);
        }
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        HttpServletRequest request = (HttpServletRequest)event.getSuppliedRequest();
        if (request == null || event.getThrowable() == null) {
            return;
        }
        Context ctx = (Context)request.getAttribute(CommonFilter.ATTR_CONTEXT);
        if (ctx != null) {
            Tracer.traceContext(event.getThrowable(), 1, ctx);
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
    }

}
