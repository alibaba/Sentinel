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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutomaticRuleManager {

    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String, Integer> qpsRecord = new ConcurrentHashMap<String, Integer>();

    private static Map<String, Double> minRTRecord = new ConcurrentHashMap<String, Double>();

    private static float maxSystemLoad = 50;

    private static boolean updateFlag = false;

    static public void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized)
            throws BlockException {

        String resourceName = resource.getName();
        FlowRule rule;
        //找出当前资源的 rule，如果不存在则创建
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

        //更新各资源的统计数据
        for(String resourceName : rules.keySet()){
            qpsRecord.put(resourceName,(int)(node.previousBlockQps()+node.previousPassQps()));
            minRTRecord.put(resourceName,node.minRt());
        }

        //每秒进行一次更新
        if(!updateFlag){
            updateFlag = true;
            while (true){
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
        // 分两类情况：1. 当前总流量超出最大值 2.当前总流量未超过最大值
        // 暂时不考虑其他应用造成的负载


        Set<String> resourceNameSet = rules.keySet();

        double currentLoad = 0;
        for(String resource : resourceNameSet){
            currentLoad += qpsRecord.get(resource)*minRTRecord.get(resource);

        }

        double systemUseage = currentLoad/maxSystemLoad;

        // 1. 系统能够处理所有请求时：根据各资源流量比例分配阈值
        if(systemUseage < 1){
            for(String resourceName : resourceNameSet){
                FlowRule rule = rules.get(resourceName);
                double currentQPS = qpsRecord.get(resourceName);
                double maximumQPS = currentQPS / systemUseage;
                rule.setCount(maximumQPS);
                rules.put(resourceName,rule);
            }
        }else{ //2. 请求数超过系统处理能力时：保护最大

            double M = -500;

            double[] C ={1, 1, 1, 0, 0, 0, 0, M, M, M, 0, 0, 0};

            double[] maxQPS = resolve(C ,qpsRecord.get("a"),qpsRecord.get("b"),qpsRecord.get("c"));

            rules.put("a",rules.get("a").setCount(maxQPS[0]));
            rules.put("b",rules.get("b").setCount(maxQPS[1]));
            rules.put("c",rules.get("c").setCount(maxQPS[2]));


        }
    }

    /*
        求解线性规划问题
        TODO: 自动生成参数矩阵
     */
    public static double[] resolve(double[] C,double maxA,double maxB,double maxC){
        double cA = minRTRecord.get("a");
        double cB = minRTRecord.get("b");
        double cC = minRTRecord.get("c");

        double minA = 10;
        double minB = 10;
        double minC = 10;

        double m1 = maxSystemLoad;

        double[][] A = {
                {cA, cB, cC, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, 0} ,
                {0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        };

        double[] b = {m1, minA, minB, minC, maxA, maxB, maxC};

        SimplexMethod simplexMethod = new SimplexMethod(A,C,b);
        return simplexMethod.solution();
    }





}
