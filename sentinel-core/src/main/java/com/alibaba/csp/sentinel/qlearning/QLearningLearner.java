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
 * @author ZhouYanjun
 */
public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    private int bacthNum = 200;
    private long batchTime = 20;

    public synchronized void learn(ResourceWrapper resourceWrapper, DefaultNode node) throws SystemBlockException {
        if (checkUpdate()) {
            if (containsQInfo() && qLearningMetric.isTrain()) {
                UpdateQ(node);
            }
            int bi = qLearningMetric.addBi();
            QInfo qInfo = takeAction(node);
            qLearningMetric.putHm(bi, qInfo);

        }
        if (qLearningMetric.getAction() == 0) {
            throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
        }
    }

    private boolean checkUpdate() {
        long ct = qLearningMetric.getCt();
        long t = TimeUtil.currentTimeMillis();

        qLearningMetric.addCn();

        if (ct <= 0) {
            qLearningMetric.setCt(TimeUtil.currentTimeMillis());

            return true;
        }
        if (t - ct >= batchTime || qLearningMetric.getCn() >= bacthNum) {
            qLearningMetric.setCt(TimeUtil.currentTimeMillis());
            qLearningMetric.resetCn();

            return true;
        }

        return false;
    }

    private boolean containsQInfo() {
        int bi = qLearningMetric.getBi();

        if (bi == 0) {
            return false;
        }

        if (qLearningMetric.getHm().containsKey(bi)) {
            return true;
        }

        return false;
    }

    private synchronized void UpdateQ(DefaultNode node) {

        QInfo qInfo = qLearningMetric.pushQInfo();
        String s = qInfo.getState();
        int a = qInfo.getAction();
        double u = qInfo.getUtility();

        double nextUtility = qLearningMetric.calculateUtility(node.successQps(), node.avgRt());

        int r = qLearningMetric.getReward(u, nextUtility);
        double q = qLearningMetric.getQValue(s, a);
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(), node.passQps(), node.avgRt(), 0);
        double qUpdated = qLearningMetric.updateQ(q, r, maxQ);

        qLearningMetric.setQValue(s, a, qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String state = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(), node.passQps(), node.avgRt(), 0);
        int action;
        if (qLearningMetric.isTrain()) {
            //随机选择
            action = qLearningMetric.getRandomAction();
        } else {
            //从qtable中选择
            action = qLearningMetric.policy(state);
        }
        qLearningMetric.setAction(action);

        double utility = qLearningMetric.calculateUtility(node.successQps(), node.avgRt());
        QInfo qInfo = new QInfo();
        qInfo.setQInfo(state, action, utility);

        return qInfo;
    }
}