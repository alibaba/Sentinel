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
package com.alibaba.csp.sentinel.adapter.servlet.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Util class for web servlet async mode.<br>
 * Use this to decouple some new api which are not exist in servlet before 3.0
 *
 * @author jason
 */
public final class AsyncUtil {
    private static boolean isServletBefore3 = true;
    private static Method methodGetAsyncContext = null;
    private static Method methodIsAsyncStarted = null;
    private static Method methodAddListener = null;
    private static Class<?> classAsyncListner = null;
    private static Class<?> classAsyncContext = null;
    static {
        try {
            classAsyncListner = Class.forName("javax.servlet.AsyncListener");
            classAsyncContext = Class.forName("javax.servlet.AsyncContext");
            methodIsAsyncStarted = ServletRequest.class.getMethod("isAsyncStarted");
            methodGetAsyncContext = ServletRequest.class.getMethod("getAsyncContext");
            methodAddListener = classAsyncContext.getMethod("addListener", 
                    classAsyncListner, ServletRequest.class, ServletResponse.class);
            isServletBefore3 = false;
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        } catch (ClassNotFoundException e) {
        }
    }
    
    /**
     * Whether an async servlet started
     * 
     * @param request
     * @return
     */
    public static boolean isAsyncStarted(HttpServletRequest request) {
        if (isServletBefore3) {
            return false;
        }
        try {
            return (Boolean) methodIsAsyncStarted.invoke(request);
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
        return false;
    }
    
    public static void addListener(EventListener listener, ServletRequest request) {
        if (isServletBefore3) {
            return;
        }
        if (request == null || listener == null) {
            return;
        }
        if (!classAsyncListner.isAssignableFrom(classAsyncListner)) {
            // just ignore
            return;
        }
        try {
            Object ctx = methodGetAsyncContext.invoke(request);
            if (ctx != null) {
                methodAddListener.invoke(ctx, listener, request, null);
            }
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private AsyncUtil() {}
}
