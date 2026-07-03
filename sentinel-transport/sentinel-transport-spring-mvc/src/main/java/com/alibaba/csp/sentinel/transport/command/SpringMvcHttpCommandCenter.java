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
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.transport.CommandCenter;

import java.util.Map;

/**
 * @author shenbaoyong
 */
@Spi(order = Spi.ORDER_LOWEST - 100)
public class SpringMvcHttpCommandCenter implements CommandCenter {

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void beforeStart() throws Exception {
        // Register handlers
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        SentinelApiHandlerMapping.registerCommands(handlers);
    }
}
