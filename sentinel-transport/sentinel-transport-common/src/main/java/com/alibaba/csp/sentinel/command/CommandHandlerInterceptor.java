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
package com.alibaba.csp.sentinel.command;

/**
 * Intercepts specified command, and can be extended using SPI.
 *
 * @author icodening
 * @since 1.8.4
 * @see com.alibaba.csp.sentinel.spi.SpiLoader
 * @see com.alibaba.csp.sentinel.spi.Spi
 */
public interface CommandHandlerInterceptor<R> {

    /**
     * whether to intercept the specified command
     *
     * @param commandName command name, eg. getRules
     * @return "true" means intercept, "false" means skip
     */
    boolean shouldIntercept(String commandName);

    /**
     * intercept the given command request, and return a command response
     *
     * @param request   commandRequest
     * @param execution interceptor chain execution
     * @return command response
     */
    CommandResponse<R> intercept(CommandRequest request, CommandRequestExecution<R> execution);

}
