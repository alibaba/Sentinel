package com.alibaba.csp.sentinel.slots.automatic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomaticRuleManager {

    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String, Integer> qpsRecord = new ConcurrentHashMap<String, Integer>();

    private static Map<String, Double> minRTRecord = new ConcurrentHashMap<String, Double>();

    private static AtomicBoolean updating = new AtomicBoolean(false);

    private static long latestUpdate;

    private static float maxUseage = 1;

    private static float minFlow = 10;

    public static int defaultCount = 125;

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

    /*
        每秒更新流控规则
     */
    static void update(ResourceWrapper resource, Context context, Node node){


        //更新资源的统计数据

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

            latestUpdate = System.currentTimeMillis();

            //更新 rules
            try {
                updateRulesByQPS();
            } catch (NullPointerException e) {
                //TODO：在第一次计算rules时由于qps为null会导致NPE

            } finally {
                updating.set(false);
            }

        }

    }

    static private void updateRulesByQPS(){
        // 分两类情况：1. 当前总流量超出最大值 2.当前总流量未超过最大值
        // 暂不考虑其他应用造成的负载
        // 暂时使用minRT作为负载系数来计算服务给系统造成的负载


        Set<String> resourceNameSet = rules.keySet();
        int resourceNum = resourceNameSet.size();

        double currentUseage = 0;
        for(String resource : resourceNameSet){
            currentUseage += qpsRecord.get(resource)*minRTRecord.get(resource)*0.001;
        }

        System.out.println("currentUseage: "+currentUseage);


        double useageLevel = currentUseage/ maxUseage;


        // 1. 系统能够处理所有请求时：根据各服务流量比例分配阈值
        if(useageLevel < 1){
            for(String resourceName : resourceNameSet){
                FlowRule rule = rules.get(resourceName);
                double currentQPS = qpsRecord.get(resourceName);
                double maximumQPS = currentQPS / useageLevel;
                rule.setCount(maximumQPS);
                rules.put(resourceName,rule);
            }
        }else{ //2. 请求数超过系统处理能力时（流量洪峰）：在保护其他服务正常访问的前提下尽可能将流量分配到发生洪峰的服务


            // 价值系数
            double[] c = new double[resourceNum*3+1];

            for( int i =0;i<resourceNum;i++){
                c[i] = 1;
            }

            //资源常数矩阵

            //最大值约束
            double[] qps = new double[resourceNum];


            int i =0;
            for(String resourceName : resourceNameSet){
                qps[i] = qpsRecord.get(resourceName);
                i+=1;
            }

            //最小值约束
            double[] b = new double[resourceNum *2 +1];
            b[0] = maxUseage;
            for(int j =0;j<resourceNum;j++ ){
                b[j+1] = qps[j];
                b[j+1+resourceNum] = minFlow;
            }

            //tableaux
            double[][] a = new double[b.length][c.length];
            //第一行：机器性能约束
            i = 0;
            for(String resourceName : resourceNameSet){
                a[0][i] = minRTRecord.get(resourceName);
                i += 1;
            }
            a[0][2*resourceNum]=1;

            //后 resourceNum 行:最小值约束
            i = 0;
            for(String resourceName : resourceNameSet){
                a[i+1][i] = 0;
                a[i+1][i+resourceNum]=-1;
                a[i+1][i+resourceNum*2+1]=1;
                i+=1;
            }
            //后 resourceNum 行:最大值约束
            i=0;
            for(String resourceName : resourceNameSet){
                a[i+resourceNum+1][i] = 0;
                a[i+resourceNum+1][i+resourceNum*3+1]=1;
                i+=1;
            }

            TwoPhaseSimplex lp = new TwoPhaseSimplex(a,b,c);

            double[] x = lp.primal();
            i = 0;
            for(String resourceName : resourceNameSet){
                rules.put(resourceName,rules.get(resourceName).setCount(x[i]));
                i+=1;
            }

        }
    }


    static public boolean canPass(Node node, int acquireCount, FlowRule rule) {
        int curCount = (int)(node.passQps());

        if (curCount + acquireCount > rule.getCount()) {

            return false;
        }
        return true;
    }

}
