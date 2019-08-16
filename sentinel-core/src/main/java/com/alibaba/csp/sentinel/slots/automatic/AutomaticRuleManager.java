package com.alibaba.csp.sentinel.slots.automatic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController;
import com.alibaba.csp.sentinel.slots.system.SystemStatusListener;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomaticRuleManager {

    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String, Integer> qpsRecord = new ConcurrentHashMap<String, Integer>();

    private static Map<String, Integer> passedRecord = new ConcurrentHashMap<String, Integer>();

    private static Map<String, Double> minRTRecord = new ConcurrentHashMap<String, Double>();

    private static Map<String, Double> avgRTRecord = new ConcurrentHashMap<String, Double>();

    private static Map<String,Integer> degradeTimer = new ConcurrentHashMap<String, Integer>();

    private static double cpuUseageRecord;

    private static AtomicBoolean updating = new AtomicBoolean(false);

    private static long latestUpdate;

    private static float maxSystemUseage = -0.5F;

    private static SystemStatusListener statusListener = null;

    public static int defaultCount = 125;

    static {
        statusListener = new SystemStatusListener();
    }


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
            rule.setGrade(1);
            rule.setRater(new DefaultController(rule.getCount(),rule.getGrade()));
            rules.put(resourceName,rule);
            degradeTimer.put(resourceName, 0 );
        }else{
            rule = rules.get(resourceName);
        }


//        boolean canPass = canPass(context.getCurNode(), count, rule);
        boolean canPass = rule.getRater().canPass(context.getCurNode(),count);
        if(!canPass)
            throw new FlowException(rule.getLimitApp(), rule);

    }

    /**
     * 统计监控数据
     * 每秒更新一次流控规则
     */
    static void update(ResourceWrapper resource, Context context, Node node){

        //更新资源的统计数据
        String resourceName = resource.getName();

        int totalQps = (int) (node.previousBlockQps() + node.previousPassQps());
        qpsRecord.put(resourceName, totalQps);

        int passedQps = (int)node.previousPassQps();
        passedRecord.put(resourceName,passedQps);

        double minRt = node.minRt();
        if(minRt <= 0)
            minRt = 1;
        minRTRecord.put(resourceName, minRt );

        avgRTRecord.put(resourceName,node.avgRt());

        cpuUseageRecord = statusListener.getCpuUsage();

        //每秒更新一次rules
        if (System.currentTimeMillis() - latestUpdate < 1000)
            return;

        if (updating.compareAndSet(false, true)) {

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
    }

    /**
     * 判断各资源的状态并计算出流控阈值
     */
    static private void updateRulesByQPS(){
        // 分两类情况：1. 当前总流量超出最大值 2.当前总流量未超过最大值
        // 暂不考虑其他应用造成的负载
        // 暂时使用minRT作为负载系数来计算服务给系统造成的负载

        Set<String> resourceNameSet = rules.keySet();

        //计算系统其他应用使用的负载 ( otherAppCpuUseage =  totalCpuUseage - sum(minRT*passedQPS) )
        double otherAppUseage = 0;
        for(String resource : resourceNameSet){
            otherAppUseage += getInt(passedRecord,resource)*getDouble(minRTRecord,resource)*0.001;
        }
        otherAppUseage = statusListener.getCpuUsage() - otherAppUseage;

        //计算当前流量需要的负载 ( currentCpuUseage = sum(minRT*totalQps) )
        double currentCpuUseage = 0;
        for(String resource : resourceNameSet){
            currentCpuUseage += getInt(qpsRecord,resource)*getDouble(minRTRecord,resource)*0.001;
        }

        //计算当前可用的负载 ( aviliableCpuUseage = maxCpuUseage - otherAppCpuUseage )
        double aviliableUseage = maxSystemUseage - otherAppUseage;

        double useageLevel = currentCpuUseage / aviliableUseage;

        //熔断降级

        //判断是否有资源处于异常需要被降级
        for(String resource:resourceNameSet){
            if(getDouble(avgRTRecord,resource)> 50 && degradeTimer.get(resource) == 0)
                degradeTimer.put(resource,5);
        }

        //
        TreeSet<String> activeResources = new TreeSet<String>();
        //将正常状态的资源加入规则计算的集合，异常的资源流量降级
        for(String resource:resourceNameSet){
            if( degradeTimer.get(resource) == 0)
                activeResources.add(resource);
            else{
                rules.put(resource,rules.get(resource).setCount(0));
                degradeTimer.put(resource,degradeTimer.get(resource)-1);
            }
        }

        // 1. 系统能够处理所有请求时：根据各服务流量比例分配阈值
        if(useageLevel < 1){
            for(String resourceName : activeResources){
                FlowRule rule = rules.get(resourceName);
                double currentQPS = getInt(qpsRecord,resourceName);
                double maximumQPS = currentQPS / useageLevel;
                rule.setCount(maximumQPS);
                rules.put(resourceName,rule);
            }
        }else{//2. 请求数超过系统处理能力时（流量洪峰）：在保护其他服务正常访问的前提下尽可能将流量分配到发生洪峰的服务


            double[] maxQPS = resolve(activeResources);

            int i = 0;
            for(String resourceName : activeResources){
                rules.put(resourceName,rules.get(resourceName).setCount(maxQPS[i]));
                i+=1;
            }

        }
        updateRaters();
    }

    /**
     * 根据资源生成单纯形表并求解线性规划问题
     */
    private static double[] resolve(Set<String> activeResources){

        int resourceNum = activeResources.size();

        Set<String> resourceNameSet = activeResources;

        // 价值系数 C
        double[] c = new double[resourceNum*4+1];

        for( int i =0;i<resourceNum;i++){
            c[i] = 1;
        }
        for( int i =resourceNum*2+1;i<resourceNum*3+1;i++){
            c[i] = -500;
        }

        //资源常数矩阵 B

        //最大值约束
        double[] qps = new double[resourceNum];


        int i =0;
        for(String resourceName : resourceNameSet){
            qps[i] = qpsRecord.get(resourceName);
            i+=1;
        }

        //最小值约束
        double[] b = new double[resourceNum *2 +1];
        b[0] = 1;
        for(int j =0;j<resourceNum;j++ ){
            b[j+1] = 10;
            b[j+1+resourceNum] = qps[j];
        }


        //tableaux A
        double[][] a = new double[b.length][c.length];
        //第一行：机器性能约束
        i = 0;
        for(String resourceName : resourceNameSet){
            a[0][i] = minRTRecord.get(resourceName)*0.001;
            i += 1;
        }
        a[0][2*resourceNum]=1;

        //后 resourceNum 行:最小值约束
        i = 0;
        for(String resourceName : resourceNameSet){
            a[i+1][i] = 1;
            a[i+1][i+resourceNum]=-1;
            a[i+1][i+resourceNum*2+1]=1;
            i+=1;
        }
        //后 resourceNum 行:最大值约束
        i=0;
        for(String resourceName : resourceNameSet){
            a[i+resourceNum+1][i] = 1;
            a[i+resourceNum+1][i+resourceNum*3+1]=1;
            i+=1;
        }


        TwoPhaseSimplex lp1 = new TwoPhaseSimplex(a, b, c);

        double[] x = lp1.primal();

        return Arrays.copyOf(x,3);
    }

    /**
     * 将 FlowRule.count 更新到rater中
     */
    private static void updateRaters(){
        for(String resourceName:rules.keySet()){
            FlowRule rule = rules.get(resourceName);
            rule.setRater(new DefaultController(rule.getCount(), rule.getGrade()));
            rules.put(resourceName,rule);
        }
    }

    /**
     *  防止在资源规则已经创建但是还没有资源的监控数据时进行流量计算NPE
     *  int 类型存放 QPS 最小值为 0
     *  double 类型存放 RT 最小值为 1
     */
    private static int getInt(Map<String,Integer> record,String key){
        if(record.get(key)== null)
            return 0;
        else return record.get(key);
    }

    private static double getDouble(Map<String,Double> record,String key){
        if(record.get(key)== null)
            return 1;
        else return record.get(key);
    }

}
