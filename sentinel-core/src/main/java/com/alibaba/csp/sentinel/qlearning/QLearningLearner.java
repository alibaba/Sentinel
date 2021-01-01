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

import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 提供了实现Q-Learning算法迭代更新的方法
 * @author ZhouYanjun
 */
public class QLearningLearner {
    QLearningMetric qLearningMetric = QLearningMetric.getInstance();
    private int bachNums = 200;
    private long batchTime = 20;
    private double cpuUsageThreshold = 0.6;

    /**
     * Apply Q-Learning algorithm.
     * @param resourceWrapper the resource.
     * @param node the node.
     * @throws SystemBlockException when action is block.
     */
    public synchronized void learn(ResourceWrapper resourceWrapper, DefaultNode node) throws SystemBlockException {
        if (!qLearningMetric.isLearning()) {
            return;
        }
        /**
         * 如果使用Cpu状态，仅当CpuUsage大于阈值时，训练或测试Q-Learning决策
         */
        if (!qLearningMetric.cpuUsageIsChecked || SystemRuleManager.getCurrentCpuUsage() >= cpuUsageThreshold) {
            try {
                if (checkUpdate()) {
                    if (containQInfo() && qLearningMetric.isTrain()) {
                        updateQ(node);
                    }
                    int batchIndex = qLearningMetric.addBatchIndex();
                    QInfo qInfo = takeAction(node);
                    qLearningMetric.putQInfo(batchIndex, qInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (qLearningMetric.getAction() == 0) {
                throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
            }
        }
    }

    /**
     * According to the time spent and numbers of requests in the current batch, decide whether to update or not.
     * @return if update
     */
    private boolean checkUpdate() {
        long lastTime = qLearningMetric.getLastTime();
        long currentTime = TimeUtil.currentTimeMillis();

        qLearningMetric.addCountNum();
        if (lastTime <= 0) {
            qLearningMetric.setLastTime(TimeUtil.currentTimeMillis());
            return true;
        }
        if (currentTime - lastTime >= batchTime || qLearningMetric.getCountNum() >= bachNums) {
            qLearningMetric.setLastTime(TimeUtil.currentTimeMillis());
            qLearningMetric.resetCountNum();
            return true;
        }
        return false;
    }

    private boolean containQInfo() {
        int batchIndex = qLearningMetric.getBatchIndex();
        if (batchIndex == 0) {
            return false;
        }
        if (qLearningMetric.getQInfoHashMap().containsKey(batchIndex)) {
            return true;
        }
        return false;
    }

    /**
     * Calculate new q value and update it to the Q-Table.
     * @param node
     */
    private synchronized void updateQ(DefaultNode node) {
        QInfo qInfo = qLearningMetric.pushQInfo();
        String qInfoState = qInfo.getState();
        int qInfoAction = qInfo.getAction();
        double qInfoUtility = qInfo.getUtility();

        double nextUtility = qLearningMetric.calculateUtility(node.successQps(), node.avgRt());

        int reward = qLearningMetric.getReward(qInfoAction, qInfoUtility, nextUtility);

        double q = qLearningMetric.getQValue(qInfoState, qInfoAction);
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(), node.passQps(), node.avgRt(), 0);
        double qUpdated = qLearningMetric.updateQ(q, reward, maxQ);

        qLearningMetric.setQValue(qInfoState, qInfoAction, qUpdated);
    }

    /**
     * Set action value according to the state.
     * @param node
     * @return state, action and utility before the action is taken, which need to be recorded.
     */
    private synchronized QInfo takeAction(DefaultNode node) {
        String state = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(), node.passQps(), node.avgRt(), 0);
        int action;
        if (qLearningMetric.isTrain()) {
            //训练时，随机选择
            action = qLearningMetric.getRandomAction();
        } else {
            //测试时，根据Q-Table，选择最优决策
            action = qLearningMetric.getOptimalPolicy(state);
        }
        qLearningMetric.setAction(action);

        double utility = qLearningMetric.calculateUtility(node.successQps(), node.avgRt());
        QInfo qInfo = new QInfo();
        qInfo.setQInfo(state, action, utility);

        return qInfo;
    }
}