package com.alibaba.csp.sentinel.slots.automatic;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController;
import com.alibaba.csp.sentinel.slots.system.SystemStatusListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AutomaticRuleManager 维护一套监控数据记录和流控规则
 * 流控规则根据实时流量动态计算
 *
 * @author Li Yudong
 */
public class AutomaticRuleManager {

    /**
     * 流控规则
     */
    private static Map<String, FlowRule> rules = new ConcurrentHashMap<String, FlowRule>();

    private static Map<String,Node> nodes =new ConcurrentHashMap<String,Node>();

    /**
     * 熔断资源计时器
     */
    private static Map<String, Integer> degradeTimer = new ConcurrentHashMap<String, Integer>();

    private static double systemLoadRecord;

    private static AtomicBoolean updating = new AtomicBoolean(false);

    private static long latestUpdateTime;

    private static SystemStatusListener statusListener = null;

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-automatic-status-record-task", true));

    static {
        statusListener = new SystemStatusListener();
        scheduler.scheduleAtFixedRate(statusListener, 5, 1, TimeUnit.SECONDS);
    }


    static public void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized)
            throws BlockException {

        String resourceName = resource.getName();

        FlowRule rule;
        // 找出当前资源的 rule，如果不存在则创建
        if (rules.get(resourceName) == null) {
            rule = new FlowRule(resourceName);
            // 设置初始值
            rule.setCount(AutomaticConfiguration.DEFAULT_COUNT);
            rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setRater(new DefaultController(rule.getCount(), rule.getGrade()));
            rules.put(resourceName, rule);
            degradeTimer.put(resourceName, 0);
        } else {

            rule = rules.get(resourceName);

        }

        boolean canPass = rule.getRater().canPass(context.getCurNode(), count);
        if (!canPass) {
            throw new FlowException(rule.getLimitApp(), rule);
        }

    }

    /**
     * 统计监控数据
     * 每秒更新一次流控规则
     */
    static void update(ResourceWrapper resource, Context context, Node node) {

        // 更新并保存资源的监控数据
        String resourceName = resource.getName();

        int totalQps = (int) (node.previousBlockQps() + node.previousPassQps());
        AutomaticStatistics.setTotalQps(resourceName,totalQps);

        int passedQps = (int) node.previousPassQps();
        AutomaticStatistics.setPassedQps(resourceName,passedQps);

        double minRt = node.minRt();
        AutomaticStatistics.setMinRt(resourceName, minRt);

        double avgRt = node.avgRt();
        AutomaticStatistics.setAvgRt(resourceName,avgRt);

        systemLoadRecord = statusListener.getSystemAverageLoad();

        // 每秒更新一次 rules
        if (System.currentTimeMillis() - latestUpdateTime < AutomaticConfiguration.RULE_UPDATE_WINDOW) {
            return;
        }

        if (updating.compareAndSet(false, true)) {

            latestUpdateTime = System.currentTimeMillis();

            try {

                updateRulesByQps();

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
    static private void updateRulesByQps() {

        // 分两类情况：1. 当前总流量超出最大值 2.当前总流量未超过最大值
        // 使用minRT作为负载系数来计算服务给系统造成的负载

        Set<String> resourceNameSet = rules.keySet();

        // 计算系统负载
        // 系统其他应用使用的负载 ( otherAppLoad =  totalSystemLoad - sum(minRT*passedQPS) )
        double otherAppLoad = 0;
        for (String resource : resourceNameSet) {
            otherAppLoad += AutomaticStatistics.getPassedQps(resource) * AutomaticStatistics.getMinRt(resource) * 0.001;
        }
        otherAppLoad = systemLoadRecord - otherAppLoad;

        // 当前流量需要的负载 ( currentAppLoad = sum(minRT*totalQps) )
        double currentAppLoad = 0;
        for (String resource : resourceNameSet) {
            currentAppLoad += AutomaticStatistics.getTotalQps(resource) * AutomaticStatistics.getMinRt(resource) * 0.001;
        }

        // 当前可用的负载 ( availableLoad = maxLoad - otherAppLoad )
        double availableLoad = AutomaticConfiguration.MAX_SYSTEM_LOAD - otherAppLoad;

        double loadLevel = currentAppLoad / availableLoad;

        // 熔断降级
        // 判断是否有资源处于异常需要被降级
        for (String resource : resourceNameSet) {
            if (AutomaticStatistics.getAvgRt(resource) > AutomaticConfiguration.DEGRADE_RT && degradeTimer.get(resource) == 0) {
                degradeTimer.put(resource, AutomaticConfiguration.DEGRADE_TIME_WINDOW);
            }
        }

        TreeSet<String> activeResources = new TreeSet<String>();
        // 将正常状态的资源加入规则计算的集合，异常的资源流量降级
        for (String resource : resourceNameSet) {
            if (degradeTimer.get(resource) == 0) {
                activeResources.add(resource);
            } else {
                rules.put(resource, rules.get(resource).setCount(0));
                degradeTimer.put(resource, degradeTimer.get(resource) - 1);
            }
        }

        // 计算流控阈值
        // 1. 系统能够处理所有请求时：根据各服务流量比例分配阈值
        if (loadLevel < 1) {
            for (String resourceName : activeResources) {
                FlowRule rule = rules.get(resourceName);
                double currentQps = AutomaticStatistics.getTotalQps(resourceName);
                double maximumQps = currentQps / loadLevel;
                rule.setCount(maximumQps);
                rules.put(resourceName, rule);
            }
        }
        // 2. 请求数超过系统处理能力时（流量洪峰）：在保护其他服务正常访问的前提下尽可能将流量分配到发生洪峰的服务
        else {
            double[] maxQps = resolve(activeResources);

            int i = 0;
            for (String resourceName : activeResources) {
                rules.put(resourceName, rules.get(resourceName).setCount(maxQps[i]));
                i += 1;
            }

        }
        updateRaters();
    }

    /**
     * 根据资源生成单纯形表并求解线性规划问题
     *
     * @param activeResources 待计算流控阈值的资源
     * @return 各资源流控阈值
     */
    private static double[] resolve(Set<String> activeResources) {

        int resourceNum = activeResources.size();

        Set<String> resourceNameSet = activeResources;

        // 价值系数 C
        double[] c = new double[resourceNum * 4 + 1];

        for (int i = 0; i < resourceNum; i++) {
            c[i] = 1;
        }
        for (int i = resourceNum * 2 + 1; i < resourceNum * 3 + 1; i++) {
            c[i] = -500;
        }

        // 资源常数矩阵 B
        // 最大值约束
        double[] qps = new double[resourceNum];

        int i = 0;
        for (String resourceName : resourceNameSet) {
            qps[i] = AutomaticStatistics.getTotalQps(resourceName);
            i += 1;
        }

        // 最小值约束
        double[] b = new double[resourceNum * 2 + 1];
        b[0] = 1;
        for (int j = 0; j < resourceNum; j++) {
            b[j + 1] = AutomaticConfiguration.RESOURCE_MIN_FLOW;
            b[j + 1 + resourceNum] = qps[j];
        }


        // tableaux A
        double[][] a = new double[b.length][c.length];
        // 第一行：机器性能约束
        i = 0;
        for (String resourceName : resourceNameSet) {
            a[0][i] = AutomaticStatistics.getMinRt(resourceName) * 0.001;
            i += 1;
        }
        a[0][2 * resourceNum] = 1;

        // 后 resourceNum 行:最小值约束
        i = 0;
        for (String resourceName : resourceNameSet) {
            a[i + 1][i] = 1;
            a[i + 1][i + resourceNum] = -1;
            a[i + 1][i + resourceNum * 2 + 1] = 1;
            i += 1;
        }

        // 后 resourceNum 行:最大值约束
        i = 0;
        for (String resourceName : resourceNameSet) {
            a[i + resourceNum + 1][i] = 1;
            a[i + resourceNum + 1][i + resourceNum * 3 + 1] = 1;
            i += 1;
        }

        TwoPhaseSimplex lp1 = new TwoPhaseSimplex(a, b, c);

        double[] x = lp1.primal();

        return Arrays.copyOf(x, resourceNum);
    }

    /**
     * 将 FlowRule.count 更新到rater中
     */
    private static void updateRaters() {
        for (String resourceName : rules.keySet()) {
            FlowRule rule = rules.get(resourceName);
            rule.setRater(new DefaultController(rule.getCount(), rule.getGrade()));
            rules.put(resourceName, rule);
        }
    }

}
