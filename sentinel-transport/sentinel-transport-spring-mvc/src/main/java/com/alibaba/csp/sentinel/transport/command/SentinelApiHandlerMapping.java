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
package com.alibaba.csp.sentinel.transport.command;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shenbaoyong
 */
public class SentinelApiHandlerMapping extends AbstractHandlerMapping implements ApplicationListener {

    private static final String SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS = "org.springframework.boot.web.context.WebServerInitializedEvent";
    private static Class webServerInitializedEventClass;

    static {
        try {
            webServerInitializedEventClass = ClassUtils.forName(SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS, null);
            RecordLog.info("[SentinelApiHandlerMapping] class {} is present, this is a spring-boot app, we can auto detect port", SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS);
        } catch (ClassNotFoundException e) {
            RecordLog.info("[SentinelApiHandlerMapping] class {} is not present, this is not a spring-boot app, we can not auto detect port", SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS);
        }
    }

    final static Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<>();

    private boolean ignoreInterceptor = true;

    public SentinelApiHandlerMapping() {
        setOrder(Ordered.LOWEST_PRECEDENCE - 10);
    }

    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String commandName = request.getRequestURI();
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        CommandHandler commandHandler = handlerMap.get(commandName);
        return commandHandler != null ? new SentinelApiHandler(commandHandler) : null;
    }

    @Override
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        return ignoreInterceptor ? new HandlerExecutionChain(handler) : super.getHandlerExecutionChain(handler, request);
    }

    public void setIgnoreInterceptor(boolean ignoreInterceptor) {
        this.ignoreInterceptor = ignoreInterceptor;
    }

    public static void registerCommand(String commandName, CommandHandler handler) {
        if (StringUtil.isEmpty(commandName) || handler == null) {
            return;
        }

        if (handlerMap.containsKey(commandName)) {
            CommandCenterLog.warn("[SentinelApiHandlerMapping] Register failed (duplicate command): " + commandName);
            return;
        }

        handlerMap.put(commandName, handler);
    }

    public static void registerCommands(Map<String, CommandHandler> handlerMap) {
        if (handlerMap != null) {
            for (Map.Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                registerCommand(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (webServerInitializedEventClass != null && webServerInitializedEventClass.isAssignableFrom(applicationEvent.getClass())) {
            Integer port = null;
            try {
                BeanWrapper beanWrapper = new BeanWrapperImpl(applicationEvent);
                port = (Integer) beanWrapper.getPropertyValue("webServer.port");
            } catch (Exception e) {
                RecordLog.warn("[SentinelApiHandlerMapping] resolve port from event " + applicationEvent + " fail", e);
            }
            if (port != null && TransportConfig.getPort() == null) {
                RecordLog.info("[SentinelApiHandlerMapping] resolve port {} from event {}", port, applicationEvent);
                TransportConfig.setRuntimePort(port);
            }
        }
    }
}
