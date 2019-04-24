/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.command;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
@CommandMapping(name = "gateway/updateApiDefinitions", desc = "")
public class UpdateGatewayApiDefinitionGroupCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway API definition data error", e);
            return CommandResponse.ofFailure(e, "decode gateway API definition data error");
        }

        RecordLog.info("[API Server] Receiving data change (type: gateway API definition): {0}", data);

        String result = SUCCESS_MSG;
        List<ApiDefinition> apiDefinitions = JSONArray.parseArray(data, ApiDefinition.class);
        GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<>(apiDefinitions));
        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
}
