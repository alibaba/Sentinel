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

    public boolean isLearning = false;
    public boolean isTraining = false;
    public boolean ifCheckCPU = true;
    public final int maxTrainNum = 50000000;

    private final int[] actionValues = new int[]{0, 1};
    private String[] actionNames = new String[]{"Block", "Accept"};
    private int actionsCount = actionValues.length;

    private volatile ConcurrentHashMap<String, double[]> Qtable = new ConcurrentHashMap<>();

    private final double alpha = 0.5;//alpha控制了效用方程的qps的参数
    private final double beta = 2.5;//控制了效用方程的RT的参数

    private final double delta = 0.8;
    private final double gamma = 0.05;

    private final double tolerance = 0.1;
    private final int product = 100;

    private int rewardValue = 10;
    private int punishValue = -1;
    private final double CpuInterval = 0.05;
    private final int QpsInterval = 50;
    private final int RtInterval = 5;
    private final int ThreadInterval = 2;

    private AtomicLong lastTime = new AtomicLong(0);
    private AtomicInteger countNum = new AtomicInteger(0);

    private AtomicInteger batchInterval = new AtomicInteger(0);

    private ConcurrentHashMap<Integer, QInfo> qInfoConcurrentHashMap = new ConcurrentHashMap<>();

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

    public int getReward(int a,double u,double nextU) {
        if( u == 0 && a == 1){
            return rewardValue;
        }
        else if( u == 0 && a == 0){
            return punishValue;
        }
        double utilityIncrease = nextU - u;
        double addPercentage = utilityIncrease / Math.abs(u);
        return (int) (addPercentage * product);
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
        if (this.isTraining && this.getBatchInterval() <= this.maxTrainNum) {
            return true;
        }
        return false;
    }

    public double updateQ(double q, int r, double maxQ) {
        return q + delta * (r + gamma * maxQ - q);
    }

    public boolean isLearning() {
        return isLearning;
    }

    public void setLearning(boolean ifLearning) {
        this.isLearning = ifLearning;
    }

    public void setIfCheckCPU(boolean ifCheckCPU) {
        this.ifCheckCPU = ifCheckCPU;
    }

    public void setTraining(boolean training) {
        this.isTraining = training;
    }

    public String[] getActionNames() {
        return actionNames;
    }

    public void setQtable(ConcurrentHashMap<String, double[]> qtable) {
        Qtable = qtable;
    }

    public ConcurrentHashMap<String, double[]> getQtable() {
        return Qtable;
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

    public void addCn() {
        countNum.incrementAndGet();
    }

    public void resetCountNum() {
        countNum.set(0);
    }

    public int getBatchInterval() {
        return batchInterval.get();
    }

    public int addBi() {
        return batchInterval.incrementAndGet();
    }

    public void setBatchInterval(int i) {
        batchInterval.set(i);
    }

    public ConcurrentHashMap<Integer, QInfo> getqInfoConcurrentHashMap() {
        return qInfoConcurrentHashMap;
    }

    public void putHm(int i, QInfo qInfo) {
        this.qInfoConcurrentHashMap.put(i, qInfo);
    }

    public QInfo pushQInfo() {
        QInfo qInfo = getqInfoConcurrentHashMap().get(getBatchInterval());
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