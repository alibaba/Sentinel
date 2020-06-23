package com.alibaba.csp.sentinel.qlearning;

/**
 * @Author: Go Bear
 * @Description:Read and load information form json file
 * @Date:Created in 11:44 AM 2020/6/22
 */

// synchronized? static? final ?
public class QLearningMetric {


    private boolean isQLearning;

    final int stateA = 0;
    final int stateB = 1;
    final int stateC = 2;
    final int stateD = 3;
    final int stateE = 4;
//    final int stateF = 5;

    final int[] states = new int[]{stateA, stateB, stateC, stateD, stateE};

    String[] stateNames = new String[]{"CPU Usage: (0%, 25%)", "CPU Usage: (25%, 50%)", "CPU Usage: (50%, 75%)", "CPU Usage: (75%, 100%)", "CPU Usage: UnGet"};
    String[] actionNames = new String[]{"Block", "Accept"};

    private static volatile double utilityIncrease;
    private static volatile int state;
    private static volatile int action;

    private static volatile int statesCount = 5;
    private static volatile int actionsCount = 2;
    private static volatile double[][] Q = new double[statesCount][actionsCount];

    private static volatile int maxTrainNum = 100000;
    private static volatile boolean isTrain = true;

    private static volatile int trainNum = 0;

    private static double alpha = 1;
    private static double beta = 0.02;

    private static double delta = 1;
    private static double gamma = 1;

    private static int rewardValue = 10;
    private static int punishValue = -1;


    private QLearningMetric() {

    }

    public synchronized void setUtilityIncrease(double utilityIncrease) {
        this.utilityIncrease = utilityIncrease;
    }

    public double getUtilityIncrease() {
        return utilityIncrease;
    }

    public synchronized void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public synchronized void setQValue(int state, int action, double q) {
        Q[state][action] = q;
    }

    public double getQValue(int state, int action) {
        return this.Q[state][action];
    }


    public synchronized void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setQ(int s, int a, double value) {
        Q[s][a] = value;
    }

    public double getmaxQ(int nextState) {

        double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < actionsCount; i++) {
            double value = this.Q[nextState][i];

            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    public int getTrainNum() {
        return trainNum;
    }

    public synchronized void addTrainNum() {
        this.trainNum++;
    }

    public static int getMaxTrainNum() {
        return maxTrainNum;
    }

    public static boolean isTrain() {
        return isTrain;
    }

    public synchronized void setTrain(boolean train) {
        this.isTrain = train;
    }

    public void showPolicy() {
        System.out.println("\nshowPolicy");
        for (int i = 0; i < statesCount; i++) {
            int from = states[i];
            int to = policy(from);
            System.out.println("Current State: " + stateNames[from] + " Action: " + actionNames[to] + " Q: " + this.Q[from][to]);
        }
    }

    // get policy from state
    public int policy(int state) {
        double maxValue = Double.MIN_VALUE;
        // default goto self if not found
        int policyGotoAction = 0;

        for (int i = 0; i < actionsCount; i++) {
            int action = i;
            double value = Q[state][action];

            if (value > maxValue) {
                maxValue = value;
                policyGotoAction = action;
            }
        }
        return policyGotoAction;
    }

    public static double getAlpha() {
        return alpha;
    }

    public static double getBeta() {
        return beta;
    }

    public static double getDelta() {
        return delta;
    }

    public static double getGamma() {
        return gamma;
    }

    public static int getRewardValue() {
        return rewardValue;
    }

    public static int getPunishValue() {
        return punishValue;
    }

    public boolean isQLearning() {
        return isQLearning;
    }

    public synchronized void setQLearning(boolean QLearning) {
        isQLearning = QLearning;
    }

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }


}
