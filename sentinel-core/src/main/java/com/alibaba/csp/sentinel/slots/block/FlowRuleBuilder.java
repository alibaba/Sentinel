package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

public class FlowRuleBuilder {

    List<FlowRule> rules = new ArrayList<FlowRule>();

    public FlowRuleBuilder addRule(String app,String key,int count,int grade){
        FlowRule rule = new FlowRule();
        rule.setResource(key);
        rule.setCount(count);
        rule.setGrade(grade);
        rule.setLimitApp(app);
        rules.add(rule);
        return this;
    }

    public void load(){
        FlowRuleManager.loadRules(rules);
    }

    public void clear(){
        rules = new ArrayList<FlowRule>();
    }

    public List<FlowRule> getRules(){
        return rules;
    }

}
