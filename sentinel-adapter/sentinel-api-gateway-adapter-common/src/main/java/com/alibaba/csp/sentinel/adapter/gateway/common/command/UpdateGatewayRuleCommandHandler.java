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

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
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
@CommandMapping(name = "gateway/updateRules", desc = "Update gateway rules")
public class UpdateGatewayRuleCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway rule data error", e);
            return CommandResponse.ofFailure(e, "decode gateway rule data error");
        }

        RecordLog.info(String.format("[API Server] Receiving rule change (type: gateway rule): %s", data));

        String result = SUCCESS_MSG;
        List<GatewayFlowRule> flowRules = JSONArray.parseArray(data, GatewayFlowRule.class);
        GatewayRuleManager.loadRules(new HashSet<>(flowRules));
        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
}