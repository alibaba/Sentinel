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
package com.alibaba.csp.sentinel.transport.command.netty;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.command.handler.BasicInfoCommandHandler;
import com.alibaba.csp.sentinel.command.handler.VersionCommandHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test cases for {@link HttpServer}.
 *
 * @author cdfive
 */
public class HttpServerTest {

    private static HttpServer httpServer;

    @BeforeClass
    public static void beforeClass() {
        // Note: clear handlerMap first, as other test case may init HttpServer.handlerMap
        // if not, run mvn test, the next assertEquals(0, HttpServer.handlerMap.size()) may fail
        HttpServer.handlerMap.clear();

        // Create new HttpServer
        httpServer = new HttpServer();

        // No handler in handlerMap at first
        assertEquals(0, HttpServer.handlerMap.size());
    }

    @Before
    public void before() {
        // Clear handlerMap every method call
        HttpServer.handlerMap.clear();
    }

    @Test
    public void testRegisterCommand() {
        String commandName;
        CommandHandler handler;

        // If commandName is null, no handler added in handlerMap
        commandName = null;
        handler = new VersionCommandHandler();
        httpServer.registerCommand(commandName, handler);
        assertEquals(0, HttpServer.handlerMap.size());

        // If commandName is "", no handler added in handlerMap
        commandName = "";
        handler = new VersionCommandHandler();
        httpServer.registerCommand(commandName, handler);
        assertEquals(0, HttpServer.handlerMap.size());

        // If handler is null, no handler added in handlerMap
        commandName = "version";
        handler = null;
        httpServer.registerCommand(commandName, handler);
        assertEquals(0, HttpServer.handlerMap.size());

        // Add one handler, commandName:version, handler:VersionCommandHandler
        commandName = "version";
        handler = new VersionCommandHandler();
        httpServer.registerCommand(commandName, handler);
        assertEquals(1, HttpServer.handlerMap.size());

        // Add the same name Handler, no handler added in handlerMap
        commandName = "version";
        handler = new VersionCommandHandler();
        httpServer.registerCommand(commandName, handler);
        assertEquals(1, HttpServer.handlerMap.size());

        // Add another handler, commandName:basicInfo, handler:BasicInfoCommandHandler
        commandName = "basicInfo";
        handler = new BasicInfoCommandHandler();
        httpServer.registerCommand(commandName, handler);
        assertEquals(2, HttpServer.handlerMap.size());
    }

    @Test
    public void testRegisterCommands() {
        Map<String, CommandHandler> handlerMap = null;

        // If handlerMap is null, no handler added in handlerMap
        httpServer.registerCommands(handlerMap);
        assertEquals(0, HttpServer.handlerMap.size());

        // Add handler from CommandHandlerProvider
        handlerMap = CommandHandlerProvider.getInstance().namedHandlers();
        httpServer.registerCommands(handlerMap);
        // Check same size
        assertEquals(handlerMap.size(), HttpServer.handlerMap.size());
        // Check not same reference
        assertTrue(handlerMap != HttpServer.handlerMap);
    }
}
