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

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "cluster/server/flowRules", desc = "get cluster flow rules")
public class FetchClusterFlowRulesCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String namespace = request.getParam("namespace");
        if (StringUtil.isEmpty(namespace)) {
            return CommandResponse.ofSuccess(JSON.toJSONString(ClusterFlowRuleManager.getAllFlowRules()));
        } else {
            return CommandResponse.ofSuccess(JSON.toJSONString(ClusterFlowRuleManager.getFlowRules(namespace)));
        }
    }
}
