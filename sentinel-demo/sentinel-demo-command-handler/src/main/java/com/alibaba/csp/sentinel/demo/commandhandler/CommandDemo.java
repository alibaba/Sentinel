/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.commandhandler;

import com.alibaba.csp.sentinel.init.InitExecutor;

/**
 * <p>To run this demo, we need to add the {@code sentinel-transport-simple-http} dependency.</p>
 * <p>
 * As soon as the CommandCenter has been initialized, we can visit {@code http://ip:commandPort/api}
 * to see all available command APIs (by default the port is 8719).
 * We can also visit our customized {@code /echo} command.
 * </p>
 *
 * @author Eric Zhao
 */
public class CommandDemo {

    public static void main(String[] args) {
        // Only for demo. You don't have to do this in your application.
        InitExecutor.doInit();

        System.out.println("Sentinel CommandCenter has been initialized");
    }
}
