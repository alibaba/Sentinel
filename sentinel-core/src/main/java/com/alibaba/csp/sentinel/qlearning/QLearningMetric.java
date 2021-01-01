/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.qlearning;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 提供了获取状态值、效用、奖励值、Q值以及最优决策的方法。
 * @author ZhouYanjun
 */
public class QLearningMetric {
    /**
     * Mark whether the switches are set by users.
     */
    public boolean learningIsSet = false;
    public boolean trainingIsSet = false;
    public boolean cpuUsageIsChecked = true;

    public final int maxTrainNum = 50000000;

    private final int[] actionValues = new int[]{0, 1};
    private String[] actionNames = new String[]{"Block", "Accept"};
    private int actionsCount = actionValues.length;

    /**
     * 效用方程的参数
     */
    private final double alpha = 0.5;
    private final double beta = 2.5;

    /**
     * Q值更新算法的参数
     */
    private final double delta = 0.8;
    private final double gamma = 0.05;

    /**
     * 计算奖励值的参数
     */
    private final int product = 100;
    private int rewardValue = 10;
    private int punishValue = -1;

    /**
     * State变量的划分区间
     */
    private final double CpuInterval = 0.05;
    private final int QpsInterval = 50;
    private final int RtInterval = 5;
    private final int ThreadInterval = 2;

    /**
     * 当前批次的第一个请求的到达时间
     */
    private AtomicLong lastTime = new AtomicLong(0);
    /**
     * 当前批次的请求的累计数量
     */
    private AtomicInteger countNum = new AtomicInteger(0);
    /**
     * 当前批次的序号
     */
    private AtomicInteger batchIndex = new AtomicInteger(0);

    /**
     * Q-Table，存储Q值
     */
    private volatile ConcurrentHashMap<String, double[]> qTable = new ConcurrentHashMap<>();
    /**
     * 存储决策前的state,action,utility信息。
     */
    private ConcurrentHashMap<Integer, QInfo> qInfoHashMap = new ConcurrentHashMap<>();

    /**
     * 当前批次的决策action
     */
    private AtomicInteger action = new AtomicInteger(1);

    /**
     * 根据划分区间确定状态变量的值
     *
     * @param currentCpuUsage
     * @param totalQps
     * @param rt
     * @param curThreadNum
     * @return a state String.
     */
    public String locateState(double currentCpuUsage, double totalQps, double rt, int curThreadNum) {
        int stateC;
        if (cpuUsageIsChecked) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }

        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

        String currentState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;

        if (!qTable.containsKey(currentState)) {
            qTable.put(currentState, new double[actionsCount]);
        }

        return currentState;
    }

    public double calculateUtility(double successQps, double avgRt) {
        return alpha * successQps - beta * avgRt;
    }

    public int getReward(int a, double u, double nextU) {
        if (u == 0 && a == 1) {
            return rewardValue;
        } else if (u == 0 && a == 0) {
            return punishValue;
        }
        double utilityIncrease = nextU - u;
        double addPercentage = utilityIncrease / Math.abs(u);
        return (int) (addPercentage * product);
    }

    public double getQValue(String state, int action) {
        return this.qTable.get(state)[action];
    }

    public void setQValue(String s, int a, double value) {
        qTable.get(s)[a] = value;
    }

    /**
     * 在下一状态的所有Q值中寻找最大的Q值
     *
     * @param currentCpuUsage
     * @param totalQps
     * @param rt
     * @param curThreadNum
     * @return a max Q value of next state.
     */
    public double getMaxQ(double currentCpuUsage, double totalQps, double rt, int curThreadNum) {
        int stateC;
        if (cpuUsageIsChecked) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }

        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

        String nextState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;
        if (!qTable.containsKey(nextState)) {
            return 0;
        } else {
            double maxValue = Double.MIN_VALUE;
            double value;
            for (int i = 0; i < actionsCount; i++) {
                value = this.qTable.get(nextState)[i];
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    /**
     * 为给定状态返回一个最优的action.
     *
     * @param state
     * @return action value.
     */
    public int getOptimalPolicy(String state) {
        double maxValue = Double.MIN_VALUE;
        double value;
        int policyGotoAction = 0;
        if (!qTable.containsKey(state)) {
            return policyGotoAction;
        }
        for (int a = 0; a < actionsCount; a++) {
            value = qTable.get(state)[a];
            if (value >= maxValue) {
                maxValue = value;
                policyGotoAction = a;
            }
        }
        return policyGotoAction;
    }

    public synchronized int getRandomAction() {
        return new Random().nextInt(actionsCount);
    }

    public synchronized boolean isTrain() {
        if (this.trainingIsSet && this.getBatchIndex() <= this.maxTrainNum) {
            return true;
        }
        return false;
    }

    public double updateQ(double q, int r, double maxQ) {
        return q + delta * (r + gamma * maxQ - q);
    }

    public boolean isLearning() {
        return learningIsSet;
    }

    public void setLearningIsSet(boolean ifLearning) {
        this.learningIsSet = ifLearning;
    }

    public void setCpuUsageIsChecked(boolean cpuUsageIsChecked) {
        this.cpuUsageIsChecked = cpuUsageIsChecked;
    }

    public void setTrainingIsSet(boolean trainingIsSet) {
        this.trainingIsSet = trainingIsSet;
    }

    public String[] getActionNames() {
        return actionNames;
    }

    public void setqTable(ConcurrentHashMap<String, double[]> qTable) {
        this.qTable = qTable;
    }

    public ConcurrentHashMap<String, double[]> getqTable() {
        return qTable;
    }

    public long getLastTime() {
        return lastTime.get();
    }

    public void setLastTime(long t) {
        lastTime.set(t);
    }

    public int getCountNum() {
        return countNum.get();
    }

    public void addCountNum() {
        countNum.incrementAndGet();
    }

    public void resetCountNum() {
        countNum.set(0);
    }

    public int getBatchIndex() {
        return batchIndex.get();
    }

    public int addBatchIndex() {
        return batchIndex.incrementAndGet();
    }

    public ConcurrentHashMap<Integer, QInfo> getQInfoHashMap() {
        return qInfoHashMap;
    }

    public void putQInfo(int i, QInfo qInfo) {
        this.qInfoHashMap.put(i, qInfo);
    }

    public QInfo pushQInfo() {
        QInfo qInfo = getQInfoHashMap().get(getBatchIndex());
        return qInfo;
    }

    public int getAction() {
        return this.action.get();
    }

    public void setAction(int a) {
        this.action.set(a);
    }

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }

}