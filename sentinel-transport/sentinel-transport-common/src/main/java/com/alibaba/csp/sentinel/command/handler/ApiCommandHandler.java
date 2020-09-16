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
package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.serialization.common.JsonTransformerLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * List all available command handlers by request: </br>
 * {@code curl http://ip:commandPort/api}
 * </p>
 *
 * @author houyi
 * @since 1.5.0
 */
@CommandMapping(name = "api", desc = "get all available command handlers")
public class ApiCommandHandler implements CommandHandler<String> {
    @SuppressWarnings("unused")
    private static class Handler {
        private String url;
        private String desc;
        public Handler(String url, String desc) {
            this.url = url;
            this.desc = desc;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public String getDesc() {
            return desc;
        }
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        List<Handler> array = new ArrayList<>();
        if (handlers.isEmpty()) {
            return CommandResponse.ofSuccess(JsonTransformerLoader.serializer().serialize(array));
        }
        for (CommandHandler handler : handlers.values()) {
            CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
            if (commandMapping == null) {
                continue;
            }
            String api = commandMapping.name();
            String desc = commandMapping.desc();
            array.add(new Handler("/" + api, desc));
        }
        return CommandResponse.ofSuccess(JsonTransformerLoader.serializer().serialize(array));
    }

}
