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
 * @author ZhouYanjun
 */
public class QLearningMetric {

    public boolean ifCheckCPU = true;
    public final int maxTrainNum = 50000000;

    public boolean isLearning;

    private final int[] actionValues = new int[]{0, 1};
    private String[] actionNames = new String[]{"Block", "Accept"};
    private volatile int actionsCount = actionValues.length;

    private volatile ConcurrentHashMap<String, double[]> Qtable = new ConcurrentHashMap<>();

    private final double alpha = 0.1;//alpha控制了效用方程的qps的参数
    private final double beta = 0.01;//控制了效用方程的RT的参数

    private final double delta = 0.8;
    private final double gamma = 0.05;

    private final double tolerance = 0.1;

    private int rewardValue = 10;
    private int punishValue = -1;
    private final double CpuInterval = 0.1;
    private final int QpsInterval = 200;
    private final int RtInterval = 10;
    private final int ThreadInterval = 2;
    private int statesCount;

    private AtomicLong ct = new AtomicLong(0);
    private AtomicInteger cn = new AtomicInteger(0);

    private AtomicInteger bi = new AtomicInteger(0);

    private ConcurrentHashMap<Integer, QInfo> hm = new ConcurrentHashMap<>();

    private AtomicInteger action = new AtomicInteger(1);

    public String locateState(double currentCpuUsage, double totalQps, double Rt, int curThreadNum) {
        int stateC;
        if (ifCheckCPU) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }

        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(Rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

        String currentState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;

        if (!Qtable.containsKey(currentState)) {

            Qtable.put(currentState, new double[actionsCount]);

        }

        return currentState;
    }

    public double calculateUtility(double successQPS, double avgRt) {
        return alpha * successQPS - beta * avgRt;
    }

    public int getReward(double u, double nextU) {
        double UtilityIncrease = nextU - u;
        if (UtilityIncrease >= tolerance) {
            return rewardValue;
        } else if (UtilityIncrease <= -1 * tolerance) {
            return punishValue;
        } else {
            return 0;
        }
    }

    public double getQValue(String state, int action) {
        return this.Qtable.get(state)[action];
    }

    public void setQValue(String s, int a, double value) {
        Qtable.get(s)[a] = value;
    }

    public double getMaxQ(double currentCpuUsage, double totalQps, double Rt, int curThreadNum) {
        int stateC;
        if (ifCheckCPU) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }

        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(Rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

        String nextState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;
        if (!Qtable.containsKey(nextState)) {
            return 0;
        } else {
            double maxValue = Double.MIN_VALUE;
            double value;
            for (int i = 0; i < actionsCount; i++) {
                value = this.Qtable.get(nextState)[i];
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    public int policy(String state) {
        double maxValue = Double.MIN_VALUE;
        double value;
        int policyGotoAction = 0;
        for (int a = 0; a < actionsCount; a++) {
            value = Qtable.get(state)[a];
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
        if (this.isLearning && this.getBi() <= this.maxTrainNum) {
            return true;
        }
        return false;
    }

    public double updateQ(double q, int r, double maxQ) {
        return q + delta * (r + gamma * maxQ - q);
    }

    public void setIfCheckCPU(boolean ifCheckCPU) {
        this.ifCheckCPU = ifCheckCPU;
    }

    public void setLearning(boolean learning) {
        this.isLearning = learning;
    }

    public void setQtable(ConcurrentHashMap<String, double[]> qtable) {
        Qtable = qtable;
    }

    public ConcurrentHashMap<String, double[]> getQtable() {
        return Qtable;
    }

    public long getCt() {
        return ct.get();
    }

    public void setCt(long t) {
        ct.set(t);
    }

    public int getCn() {
        return cn.get();
    }

    public void addCn() {
        cn.incrementAndGet();
    }

    public void resetCn() {
        cn.set(0);
    }

    public int getBi() {
        return bi.get();
    }

    public int addBi() {
        return bi.incrementAndGet();
    }

    public void setBi(int i) {
        bi.set(i);
    }

    public ConcurrentHashMap<Integer, QInfo> getHm() {
        return hm;
    }

    public void putHm(int i, QInfo qInfo) {
        this.hm.put(i, qInfo);
    }

    public QInfo pushQInfo() {
        QInfo qInfo = getHm().get(getBi());
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