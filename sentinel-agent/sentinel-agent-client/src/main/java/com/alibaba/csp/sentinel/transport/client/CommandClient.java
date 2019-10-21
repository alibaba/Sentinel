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
package com.alibaba.csp.sentinel.transport.client;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;

/**
 * Basic interface for clients that sending commands.
 *
 * @author Eric Zhao
 */
public interface CommandClient {

    /**
     * Send a command to target destination.
     *
     * @param host    target host
     * @param port    target port
     * @param request command request
     * @return the response from target command server
     * @throws Exception when unexpected error occurs
     */
    CommandResponse sendCommand(String host, int port, CommandRequest request) throws Exception;
}
