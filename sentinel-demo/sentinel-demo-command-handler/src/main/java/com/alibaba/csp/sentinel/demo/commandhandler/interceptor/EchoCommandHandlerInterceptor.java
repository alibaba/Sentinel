/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.commandhandler.interceptor;

import com.alibaba.csp.sentinel.command.CommandHandlerInterceptor;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandRequestExecution;
import com.alibaba.csp.sentinel.command.CommandResponse;

/**
 * @author icodening
 * @date 2022.03.23
 */
public class EchoCommandHandlerInterceptor implements CommandHandlerInterceptor<String> {

    @Override
    public boolean shouldIntercept(String commandName) {
        return "echo".equals(commandName);
    }

    @Override
    public CommandResponse<String> intercept(CommandRequest request, CommandRequestExecution<String> execution) {
        System.out.println("[EchoCommandHandlerInterceptor] intercept 'echo' command,all params is " + request.getParameters() + "");
        CommandResponse<String> response = execution.execute(request);
        return CommandResponse.ofSuccess("intercept result : [" + response.getResult() + "]");
    }
}
