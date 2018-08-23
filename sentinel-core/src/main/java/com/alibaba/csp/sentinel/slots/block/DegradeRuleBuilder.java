package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

public class DegradeRuleBuilder {

    List<DegradeRule> rules = new ArrayList<DegradeRule>();

    public DegradeRuleBuilder addRule(String app,String key,int count,int grade,int timeWindow){
        DegradeRule rule = new DegradeRule();
        rule.setResource(key);
        rule.setCount(count);
        rule.setGrade(grade);
        rule.setLimitApp(app);
        rule.setTimeWindow(timeWindow);
        rules.add(rule);
        return this;
    }

    public void load(){
        DegradeRuleManager.loadRules(rules);
    }

    public void clear(){
        rules = new ArrayList<DegradeRule>();
    }

    public List<DegradeRule> getRules(){
        return rules;
    }
}
