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

import java.util.Map;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowSlot;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetric;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

/**
 * @author Eric Zhao
 * @since 0.2.0
 */
@CommandMapping(name = "topParams", desc = "get topN param in specified resource, accept param: res={resourceName}&idx={paramIndex}&n={topN}")
public class FetchTopParamsCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String resourceName = request.getParam("res");
        if (StringUtil.isBlank(resourceName)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Invalid parameter: res"));
        }
        String idx = request.getParam("idx");
        int index;
        try {
            index = Integer.valueOf(idx);
        } catch (Exception ex) {
            return CommandResponse.ofFailure(ex, "Invalid parameter: idx");
        }
        String n = request.getParam("n");
        int amount;
        try {
            amount = Integer.valueOf(n);
        } catch (Exception ex) {
            return CommandResponse.ofFailure(ex, "Invalid parameter: n");
        }
        ParameterMetric metric = ParamFlowSlot.getHotParamMetricForName(resourceName);
        if (metric == null) {
            return CommandResponse.ofSuccess("{}");
        }
        Map<Object, Double> values = metric.getTopPassParamCount(index, amount);

        return CommandResponse.ofSuccess(JSON.toJSONString(values));
    }
}
