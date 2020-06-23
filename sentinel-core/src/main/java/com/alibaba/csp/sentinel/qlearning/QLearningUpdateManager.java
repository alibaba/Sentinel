package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 *
 */
public class QLearningUpdateManager {
    QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    private  double alpha = qLearningMetric.getAlpha();
    private  double beta = qLearningMetric.getBeta();

    private  double delta = qLearningMetric.getDelta();
    private  double gamma = qLearningMetric.getGamma();

    private  int rewardValue = qLearningMetric.getRewardValue();
    private  int punishValue = qLearningMetric.getPunishValue();

    public  int currentState;

    public  double currentUtility;
    public  double nextUtility;
    public  double utilityIncrease;


    public  void qLearningProcess(double successQPS, double avgRt){
        //
        if(qLearningMetric.isQLearning() && qLearningMetric.isTrain()) {

            qLearningMetric.addTrainNum();
            if (qLearningMetric.getTrainNum() <= qLearningMetric.getMaxTrainNum()) {
                // 记录当前的增量。
                recordUtilityIncrease(successQPS, avgRt);
                updateQ();
            } else {
                qLearningMetric.setTrain(false);
//                System.out.println("-------------------TRAINING END--------------------");
//                qLearningMetric.showPolicy();
//                System.out.println(" ");
            }
        }
    }

    private  void recordUtilityIncrease(double successQPS, double avgRt) {

        setNextUtility(successQPS, avgRt);

        utilityIncrease = nextUtility - currentUtility;

//        System.out.println( "_____Accept____ " + nextUtility + "               CU = " + currentUtility);

        qLearningMetric.setUtilityIncrease(utilityIncrease);
    }

    private  void updateQ() {
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

    private int getReward() {
        if (qLearningMetric.getUtilityIncrease() >= 0) {
            return rewardValue;
        } else {
            return punishValue;
        }
    }


    public double getCurrentUtility() {
        return currentUtility;
    }


    public void setCurrentUtility(double successQPS,double avgRt) {
        currentUtility = alpha * successQPS - beta * avgRt;
    }

    public  double getNextUtility() {
        return nextUtility;
    }

    public  void setNextUtility(double successQPS,double avgRt) {
        nextUtility = alpha * successQPS - beta * avgRt;
    }

    public  double getUtilityIncrease() {
        return utilityIncrease;
    }

    public  void setUtilityIncrease(double utilityIncrease) {
        utilityIncrease = utilityIncrease;
    }



}
