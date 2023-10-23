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
package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.*;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * intercept specified command handler
 *
 * @author icodening
 * @date 2022.03.03
 */
public class InterceptingCommandHandler<R> implements CommandHandler<R> {

    private final CommandHandler<R> delegate;

    private final List<CommandHandlerInterceptor<R>> commandHandlerInterceptors;

    public InterceptingCommandHandler(CommandHandler<R> delegate, List<CommandHandlerInterceptor<R>> commandHandlerInterceptors) {
        AssertUtil.notNull(delegate, "delegate cannot be null");
        AssertUtil.notNull(commandHandlerInterceptors, "commandHandlerInterceptors cannot be null");
        this.delegate = delegate;
        this.commandHandlerInterceptors = commandHandlerInterceptors;
    }

    @Override
    public CommandResponse<R> handle(CommandRequest request) {
        return new InterceptingRequestExecution<>(commandHandlerInterceptors.iterator(), delegate::handle).execute(request);
    }

    private static class InterceptingRequestExecution<R> implements CommandRequestExecution<R> {

        private final Iterator<CommandHandlerInterceptor<R>> iterator;

        private final Function<CommandRequest, CommandResponse<R>> commandResponseFunction;

        public InterceptingRequestExecution(Iterator<CommandHandlerInterceptor<R>> iterator,
                                            Function<CommandRequest, CommandResponse<R>> commandResponseFunction) {
            this.iterator = iterator;
            this.commandResponseFunction = commandResponseFunction;
        }

        @Override
        public CommandResponse<R> execute(CommandRequest request) {
            if (this.iterator.hasNext()) {
                CommandHandlerInterceptor<R> nextInterceptor = this.iterator.next();
                return nextInterceptor.intercept(request, this);
            }
            return commandResponseFunction.apply(request);
        }
    }
}
