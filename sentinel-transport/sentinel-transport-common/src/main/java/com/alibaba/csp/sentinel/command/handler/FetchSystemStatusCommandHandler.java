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
package com.alibaba.csp.sentinel.command.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.fastjson.JSONObject;

/**
 * @author jialiang.linjl
 */
@CommandMapping(name = "systemStatus", desc = "get system status")
public class FetchSystemStatusCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {

        Map<String, Object> systemStatus = new HashMap<String, Object>();

        systemStatus.put("rqps", Constants.ENTRY_NODE.successQps());
        systemStatus.put("qps", Constants.ENTRY_NODE.passQps());
        systemStatus.put("b", Constants.ENTRY_NODE.blockQps());
        systemStatus.put("r", Constants.ENTRY_NODE.avgRt());
        systemStatus.put("t", Constants.ENTRY_NODE.curThreadNum());

        return CommandResponse.ofSuccess(JSONObject.toJSONString(systemStatus));
    }
}
