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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler;
import com.alibaba.fastjson.JSON;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModifyParamFlowRulesCommandHandlerTest {

    @After
    public void tearDown() {
        ParamFlowRuleManager.loadRules(Collections.<ParamFlowRule>emptyList());
    }

    @Test
    public void testHandleDecodedDataPreservesPlusInParamFlowItem() {
        ParamFlowRule rule = new ParamFlowRule("test-resource")
            .setParamIdx(0)
            .setCount(1);
        rule.setParamFlowItemList(Collections.singletonList(new ParamFlowItem("a+b", 3, String.class.getName())));

        CommandRequest request = new CommandRequest();
        request.addParam("data", JSON.toJSONString(Collections.singletonList(rule)));

        CommandResponse<String> response = new ModifyParamFlowRulesCommandHandler().handle(request);

        assertTrue(response.isSuccess());
        List<ParamFlowRule> rules = ParamFlowRuleManager.getRulesOfResource("test-resource");
        assertEquals(1, rules.size());
        assertEquals(Integer.valueOf(3), rules.get(0).getParsedHotItems().get("a+b"));
    }
}
