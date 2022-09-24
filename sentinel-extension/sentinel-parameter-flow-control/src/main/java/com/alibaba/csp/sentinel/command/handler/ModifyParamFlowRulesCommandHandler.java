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

import java.net.URLDecoder;
import java.util.List;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;

/**
 * @author Eric Zhao
 * @since 0.2.0
 */
@CommandMapping(name = "setParamFlowRules", desc = "Set parameter flow rules, while previous rules will be replaced.")
public class ModifyParamFlowRulesCommandHandler implements CommandHandler<String> {

    private static WritableDataSource<List<ParamFlowRule>> paramFlowWds = null;

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode rule data error", e);
            return CommandResponse.ofFailure(e, "decode rule data error");
        }

        RecordLog.info("[API Server] Receiving rule change (type:parameter flow rule): {}", data);

        String result = SUCCESS_MSG;
        List<ParamFlowRule> flowRules = JSONArray.parseArray(data, ParamFlowRule.class);
        ParamFlowRuleManager.loadRules(flowRules);
        if (!writeToDataSource(paramFlowWds, flowRules)) {
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

    public synchronized static WritableDataSource<List<ParamFlowRule>> getWritableDataSource() {
        return paramFlowWds;
    }

    public synchronized static void setWritableDataSource(WritableDataSource<List<ParamFlowRule>> hotParamWds) {
        ModifyParamFlowRulesCommandHandler.paramFlowWds = hotParamWds;
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";
}
