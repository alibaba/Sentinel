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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        JSONArray array = new JSONArray();
        if (handlers.isEmpty()) {
            return CommandResponse.ofSuccess(array.toJSONString());
        }
        for (CommandHandler handler : handlers.values()) {
            CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
            if (commandMapping == null) {
                continue;
            }
            String api = commandMapping.name();
            String desc = commandMapping.desc();
            JSONObject obj = new JSONObject();
            obj.put("url", "/" + api);
            obj.put("desc", desc);
            array.add(obj);
        }
        return CommandResponse.ofSuccess(array.toJSONString());
    }

}
