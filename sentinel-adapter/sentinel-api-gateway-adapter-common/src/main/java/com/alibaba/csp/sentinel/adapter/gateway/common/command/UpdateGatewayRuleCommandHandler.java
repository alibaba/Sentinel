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

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.net.URLDecoder;
import java.util.Set;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
@CommandMapping(name = "gateway/updateRules", desc = "Update gateway rules")
public class UpdateGatewayRuleCommandHandler implements CommandHandler<String> {
    private static WritableDataSource<Set<GatewayFlowRule>> gatewayFlowWds = null;

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

        RecordLog.info("[API Server] Receiving rule change (type: gateway rule): {}", data);

        String result = SUCCESS_MSG;
	    Set<GatewayFlowRule> flowRules = JSON.parseObject(data, new TypeReference<Set<GatewayFlowRule>>() {
	    });
        GatewayRuleManager.loadRules(flowRules);
        if (!writeToDataSource(gatewayFlowWds, flowRules)) {
            result = WRITE_DS_FAILURE_MSG;
        }
        return CommandResponse.ofSuccess(result);
    }

    /**
     * Write target value to given data source.
     *
     * @param dataSource writable data source
     * @param value target value to save
     * @param <T> value type
     * @return true if write successful or data source is empty; false if error occurs
     */
    private <T> boolean writeToDataSource(WritableDataSource<T> dataSource, T value) {
        if (dataSource != null) {
            try {
                dataSource.write(value);
            } catch (Exception e) {
                RecordLog.warn("Write data source failed", e);
                return false;
            }
        }
        return true;
    }

    public synchronized static WritableDataSource<Set<GatewayFlowRule>> getWritableDataSource() {
        return gatewayFlowWds;
    }

    public synchronized static void setWritableDataSource(WritableDataSource<Set<GatewayFlowRule>> gatewayFlowWds) {
        UpdateGatewayRuleCommandHandler.gatewayFlowWds = gatewayFlowWds;
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";
}
