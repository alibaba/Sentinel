package com.alibaba.csp.sentinel.slots.automatic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleChecker;
import com.alibaba.csp.sentinel.util.function.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutomaticRuleManager {

    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String, Integer> qpsRecord = new ConcurrentHashMap<String, Integer>();

    private static boolean updateFlag = false;

    static public void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized)
            throws BlockException {

        String resourceName = resource.getName();
        FlowRule rule;
        //找出当前资源的 rule，如果不存在则创建rule
        if(rules.get(resourceName)==null){
            rule = new FlowRule(resourceName);
            // 初始化限流策略
            rule.setCount(100);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setLimitApp("default");
            rules.put(resourceName,rule);
        }else
            rule = rules.get(resourceName);

        boolean canPass = rule.getRater().canPass(node, count, prioritized);
        if(!canPass)
            throw new FlowException(rule.getLimitApp(), rule);

    }

    static void update(Node node){
        //每秒进行一次更新
        if(!updateFlag){
            updateFlag = true;
            while (true){
                //获取各资源的流量
                for(String resourceName : rules.keySet()){
                    qpsRecord.put(resourceName,(int)(node.previousBlockQps()+node.previousPassQps()));
                }
                //更新 rules
                updateRulesByQPS();

                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){

                }
            }
        }
    }

    static private void updateRulesByQPS(){

    }



}
