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
package com.alibaba.csp.sentinel.cluster.server.command.handler;

import java.net.URLDecoder;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "cluster/server/modifyFlowRules", desc = "modify cluster flow rules")
public class ModifyClusterFlowRulesCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String namespace = request.getParam("namespace");
        if (StringUtil.isEmpty(namespace)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("empty namespace"));
        }
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("empty data"));
        }
        try {
            data = URLDecoder.decode(data, "UTF-8");
            RecordLog.info("[ModifyClusterFlowRulesCommandHandler] Receiving cluster flow rules for namespace <{}>: {}", namespace, data);

            List<FlowRule> flowRules = JSONArray.parseArray(data, FlowRule.class);
            ClusterFlowRuleManager.loadRules(namespace, flowRules);

            return CommandResponse.ofSuccess(SUCCESS);
        } catch (Exception e) {
            RecordLog.warn("[ModifyClusterFlowRulesCommandHandler] Decode cluster flow rules error", e);
            return CommandResponse.ofFailure(e, "decode cluster flow rules error");
        }
    }

    private static final String SUCCESS = "success";
}
