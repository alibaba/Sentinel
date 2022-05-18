package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeItem;
import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeRuleManager;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetParamDegradeRulesCommandHandlerTest {

    GetParamDegradeRulesCommandHandler handler = new GetParamDegradeRulesCommandHandler();

    @Test
    public void testGet() {
        final String resA = "resA";
        ParamDegradeRule ruleA = new ParamDegradeRule(resA).setParamIdx(0);
        ruleA.setCount(1d);
        ruleA.setSlowRatioThreshold(0.9d);
        ruleA.setTimeWindow(20);
        ruleA.setStatIntervalMs(20000);

        ParamDegradeItem item = new ParamDegradeItem();
        item.setObject("1");
        item.setCount(2d);
        item.setClassType("java.util.String");

        ParamDegradeItem item2 = new ParamDegradeItem();
        item2.setObject("2");
        item2.setCount(3d);
        item2.setClassType("java.util.String");

        List<ParamDegradeItem> itemList = new ArrayList<>();
        itemList.add(item);
        itemList.add(item2);

        ruleA.setParamDegradeItemList(itemList);

        ParamDegradeRuleManager.loadRules(Collections.singletonList(ruleA));

        CommandResponse response = handler.handle(null);

        List<ParamDegradeRule> rules = JSON.parseArray(String.valueOf(response.getResult()), ParamDegradeRule.class);
        assertTrue(rules.size() == 1);
        assertEquals(ruleA, rules.get(0));
    }
}
