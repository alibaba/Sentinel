package com.alibaba.csp.sentinel.slots.automatic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleChecker;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomaticRuleManager {

    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String, Integer> qpsRecord = new ConcurrentHashMap<String, Integer>();

    private static Map<String, Double> minRTRecord = new ConcurrentHashMap<String, Double>();

    private static AtomicBoolean updating = new AtomicBoolean(false);

    private static long latestUpdate;

    private static float maxSystemLoad = 1;

    public static int defaultCount = 125;

    static FlowRuleChecker checker= new FlowRuleChecker();

    static public void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized)
            throws BlockException {

        String resourceName = resource.getName();

        FlowRule rule;
        //找出当前资源的 rule，如果不存在则创建
        if(rules.get(resourceName)==null){
            rule = new FlowRule(resourceName);
            // 设置初始值
            rule.setCount(defaultCount);
            rule.setLimitApp("default");
            rules.put(resourceName,rule);
        }else{
            rule = rules.get(resourceName);
        }


        boolean canPass = canPass(context.getCurNode(), count, rule);

        if(!canPass)
            throw new FlowException(rule.getLimitApp(), rule);

    }

    static void update(ResourceWrapper resource, Context context, Node node){

        try {
            //更新各资源的统计数据
            //System.out.println(updating.get());

            String resourceName = resource.getName();

            int totalQps = (int) (node.previousBlockQps() + node.previousPassQps());
            qpsRecord.put(resourceName, totalQps);
            double minRt = node.minRt();
            if(minRt <= 0)
                minRt = 0.1;
            minRTRecord.put(resourceName, minRt );


            //每秒进行一次更新
            if (System.currentTimeMillis() - latestUpdate < 1000)
                return;

            if (updating.compareAndSet(false, true)) {

                System.out.println("resourceName\tqps\trule");
                for (String resourceName1 : rules.keySet()) {
                    System.out.print(resourceName1 + "\t" + qpsRecord.get(resourceName1) + "\t" + rules.get(resourceName1).getCount());
                    System.out.println();

                }

                latestUpdate = System.currentTimeMillis();
                //更新 rules

                try {
                    updateRulesByQPS();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    updating.set(false);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static private void updateRulesByQPS(){
        // 分两类情况：1. 当前总流量超出最大值 2.当前总流量未超过最大值
        // 暂不考虑其他应用造成的负载
        // 暂时使用minRT作为负载系数来计算服务给系统造成的负载


        Set<String> resourceNameSet = rules.keySet();

        double currentLoad = 0;
        for(String resource : resourceNameSet){
            currentLoad += qpsRecord.get(resource)*minRTRecord.get(resource)*0.001;
        }

        System.out.println("currentLoad: "+currentLoad);


        double systemUseage = currentLoad/maxSystemLoad;


        // 1. 系统能够处理所有请求时：根据各服务流量比例分配阈值
        if(systemUseage < 1){
            for(String resourceName : resourceNameSet){
                FlowRule rule = rules.get(resourceName);
                double currentQPS = qpsRecord.get(resourceName);
                double maximumQPS = currentQPS / systemUseage;
                rule.setCount(maximumQPS);
                rules.put(resourceName,rule);
            }
        }else{ //2. 请求数超过系统处理能力时（流量洪峰）：在保护其他服务正常访问的前提下尽可能将流量分配到发生洪峰的服务

            double M = -500;

            double[] C ={1, 1, 1, 0, 0, 0, 0, M, M, M, 0, 0, 0};

            double[] maxQPS = resolve(C ,qpsRecord.get("a"),qpsRecord.get("b"),qpsRecord.get("c"));

            rules.put("a",rules.get("a").setCount(maxQPS[0]));
            rules.put("b",rules.get("b").setCount(maxQPS[1]));
            rules.put("c",rules.get("c").setCount(maxQPS[2]));

        }
    }

    static public boolean canPass(Node node, int acquireCount, FlowRule rule) {
        int curCount = (int)(node.passQps());

        if (curCount + acquireCount > rule.getCount()) {

            return false;
        }
        return true;
    }

    /*
        求解线性规划问题
        TODO: 自动生成参数矩阵
     */
    public static double[] resolve(double[] C,double maxA,double maxB,double maxC){

        double cA = 0.001f;
        double cB = 0.001f;
        double cC = 0.001f;

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
