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
package com.alibaba.csp.sentinel.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Provides and filters command handlers registered via SPI.
 *
 * @author Eric Zhao
 */
public class CommandHandlerProvider implements Iterable<CommandHandler> {

    /**
     * com.alibaba.csp.sentinel.command.handler.BasicInfoCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchActiveRuleCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchClusterNodeByIdCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchClusterNodeHumanCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchJsonTreeCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchOriginCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchSimpleClusterNodeCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchSystemStatusCommandHandler
     * com.alibaba.csp.sentinel.command.handler.FetchTreeCommandHandler
     * com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler
     * com.alibaba.csp.sentinel.command.handler.OnOffGetCommandHandler
     * com.alibaba.csp.sentinel.command.handler.OnOffSetCommandHandler
     * com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler
     * com.alibaba.csp.sentinel.command.handler.VersionCommandHandler
     * com.alibaba.csp.sentinel.command.handler.cluster.FetchClusterModeCommandHandler
     * com.alibaba.csp.sentinel.command.handler.cluster.ModifyClusterModeCommandHandler
     * com.alibaba.csp.sentinel.command.handler.ApiCommandHandler
     */
    private final ServiceLoader<CommandHandler> serviceLoader = ServiceLoader.load(CommandHandler.class);

    /**
     * Get all command handlers annotated with {@link CommandMapping} with command name.
     *
     * @return list of all named command handlers
     */
    public Map<String, CommandHandler> namedHandlers() {
        Map<String, CommandHandler> map = new HashMap<String, CommandHandler>();
        for (CommandHandler handler : serviceLoader) {
            String name = parseCommandName(handler);
            if (!StringUtil.isEmpty(name)) {
                // 根据CommandMapping为key，handler为value放入map
                map.put(name, handler);
            }
        }
        return map;
    }

    private String parseCommandName(CommandHandler handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping != null) {
            return commandMapping.name();
        } else {
            return null;
        }
    }

    @Override
    public Iterator<CommandHandler> iterator() {
        return serviceLoader.iterator();
    }

    private static final CommandHandlerProvider INSTANCE = new CommandHandlerProvider();

    public static CommandHandlerProvider getInstance() {
        return INSTANCE;
    }
}
