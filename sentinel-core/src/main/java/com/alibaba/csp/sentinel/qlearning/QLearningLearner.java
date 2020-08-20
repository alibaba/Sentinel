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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author ZhouYanjun
 */
public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    private int bacthNum = 200;
    private long batchTime = 20;

    public synchronized void learn(ResourceWrapper resourceWrapper, DefaultNode node) throws SystemBlockException {
        if(!qLearningMetric.isLearning()){
            return;
        }
        try {
            if (checkUpdate()) {
                if (containsQInfo() && qLearningMetric.isTrain()) {
                    UpdateQ(node);
                }
                int bi = qLearningMetric.addBi();
                QInfo qInfo = takeAction(node);
                qLearningMetric.putHm(bi, qInfo);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (qLearningMetric.getAction() == 0) {
            throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
        }
    }

    private boolean checkUpdate() {
        long lastTime = qLearningMetric.getLastTime();
        long currentTime = TimeUtil.currentTimeMillis();

        qLearningMetric.addCn();

        if (lastTime <= 0) {
            qLearningMetric.setLastTime(TimeUtil.currentTimeMillis());

            return true;
        }
        if (currentTime - lastTime >= batchTime || qLearningMetric.getCountNum() >= bacthNum) {
            qLearningMetric.setLastTime(TimeUtil.currentTimeMillis());
            qLearningMetric.resetCountNum();

            return true;
        }

        return false;
    }

    private boolean containsQInfo() {
        int bi = qLearningMetric.getBatchInterval();

        if (bi == 0) {
            return false;
        }

        if (qLearningMetric.getqInfoConcurrentHashMap().containsKey(bi)) {
            return true;
        }

        return false;
    }

    private synchronized void UpdateQ(DefaultNode node) {

        QInfo qInfo = qLearningMetric.pushQInfo();
        String s = qInfo.getState();
        int a = qInfo.getAction();
        double u = qInfo.getUtility();

        double nextUtility = qLearningMetric.calculateUtility(Constants.ENTRY_NODE.successQps(), Constants.ENTRY_NODE.avgRt());

        int r = qLearningMetric.getReward(a,u, nextUtility);
        double q = qLearningMetric.getQValue(s, a);
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(), Constants.ENTRY_NODE.passQps(), Constants.ENTRY_NODE.avgRt(), 0);
        double qUpdated = qLearningMetric.updateQ(q, r, maxQ);

        qLearningMetric.setQValue(s, a, qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String state = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(), Constants.ENTRY_NODE.passQps(), Constants.ENTRY_NODE.avgRt(), 0);
        int action;
        if (qLearningMetric.isTrain()) {
            //随机选择
            action = qLearningMetric.getRandomAction();
        } else {
            //从qtable中选择
            action = qLearningMetric.policy(state);
        }
        qLearningMetric.setAction(action);

        double utility = qLearningMetric.calculateUtility(Constants.ENTRY_NODE.successQps(), Constants.ENTRY_NODE.avgRt());
        QInfo qInfo = new QInfo();
        qInfo.setQInfo(state, action, utility);

        return qInfo;
    }
}