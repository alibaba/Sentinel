/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.transport.command;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.handler.InterceptingCommandHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.util.HttpCommandUtils;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * @author icodening
 * @date 2022.03.23
 */
@SuppressWarnings("all")
public class InterceptingCommandHandlerTest {

    private Map<String, CommandHandler> commandHandlerMap = CommandHandlerProvider.getInstance().namedHandlers();

    @Before
    public void setUp() throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setClusterMode(false)
                .setCount(1)
                .setResource("/test");
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }

    @Test
    public void testInterceptCommand() {
        CommandHandler getRulesHandler = commandHandlerMap.get("getRules");
        AssertUtil.assertState(getRulesHandler instanceof InterceptingCommandHandler, "getRulesHandler should be an InterceptingCommandHandler");

        CommandHandler basicInfoHandler = commandHandlerMap.get("basicInfo");
        AssertUtil.assertState(!(basicInfoHandler instanceof InterceptingCommandHandler), "basicInfoHandler should not be an InterceptingCommandHandler");

        CommandRequest getRulesCommandRequest = new CommandRequest();
        getRulesCommandRequest.addMetadata(HttpCommandUtils.REQUEST_TARGET, "getRules");
        getRulesCommandRequest.addParam("type", "flow");
        CommandResponse getRulesResponse = getRulesHandler.handle(getRulesCommandRequest);
        System.out.println(getRulesResponse.getResult());
    }
}
