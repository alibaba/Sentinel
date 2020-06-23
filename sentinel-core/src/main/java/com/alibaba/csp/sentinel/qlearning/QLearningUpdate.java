package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

public class QLearningUpdate {
    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    private static double alpha = qLearningMetric.getAlpha();
    private static double beta = qLearningMetric.getBeta();

    private static double delta = qLearningMetric.getDelta();
    private static double gamma = qLearningMetric.getGamma();

    private static int rewardValue = qLearningMetric.getRewardValue();
    private static int punishValue = qLearningMetric.getPunishValue();

    public static int currentState;

    public static double currentUtility;
    public static double nextUtility;
    public static double utilityIncrease;


    public static void qLearningProcess(double successQPS, double avgRt){
        if (qLearningMetric.isTrain()) {
            // 记录当前的增量。
            qLearningMetric.addTrainNum();
            if (qLearningMetric.getTrainNum() <= qLearningMetric.getMaxTrainNum()) {
                recordUtilityIncrease(successQPS, avgRt);
                updateQ();
            } else {
                qLearningMetric.setTrain(false);
                System.out.println("-------------------TRAINING END--------------------");
                qLearningMetric.showPolicy();
                System.out.println(" ");
            }
        } else {

        }
    }

    private static void recordUtilityIncrease(double successQPS, double avgRt) {

        nextUtility = alpha * successQPS - beta * avgRt;

        utilityIncrease = nextUtility - currentUtility;

//        System.out.println( "_____Accept____ " + nextUtility + "               CU = " + currentUtility);

        qLearningMetric.setUtilityIncrease(utilityIncrease);
    }

    private static void updateQ() {
        int reward = getReward();
        int state = qLearningMetric.getState();
        int action = qLearningMetric.getAction();
        double q = qLearningMetric.getQValue(state, action);
        //执行action之后的下一个state属于哪个state。
//            locateNextState();

        double cpuUsage = SystemRuleManager.getCurrentCpuUsage();
        int nextState = SystemRuleManager.locateState(cpuUsage);

        double maxQ = qLearningMetric.getmaxQ(nextState);

        double qValue = q + delta * (reward + gamma * maxQ - q);
        qLearningMetric.setQ(state, action, qValue);
    }

    private static int getReward() {
        if (qLearningMetric.getUtilityIncrease() >= 0) {
            return rewardValue;
        } else {
            return punishValue;
        }
    }


    public static double getCurrentUtility() {
        return currentUtility;
    }

    /**
     //     * ???this.current / QLearningUtility ????
     * @param successQPS
     * @param avgRt
     */
    public static void setCurrentUtility(double successQPS,double avgRt) {
        currentUtility = alpha * successQPS - beta * avgRt;
        QLearningUpdate.currentUtility = currentUtility;
    }

    public static double getNextUtility() {
        return nextUtility;
    }

    public static void setNextUtility(double nextUtility) {
        QLearningUpdate.nextUtility = nextUtility;
    }

    public static double getUtilityIncrease() {
        return utilityIncrease;
    }

    public static void setUtilityIncrease(double utilityIncrease) {
        QLearningUpdate.utilityIncrease = utilityIncrease;
    }



}
