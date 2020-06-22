package com.alibaba.csp.sentinel.qlearning;

/**
 * @Author: Go Bear
 * @Description:Read and load information form json file
 * @Date:Created in 11:44 AM 2020/6/22
 */
public class QLearningMetric {

    final int stateA = 0;
    final int stateB = 1;
    final int stateC = 2;
    final int stateD = 3;
    final int stateE = 4;
//    final int stateF = 5;

    final int[] states = new int[]{stateA, stateB, stateC, stateD, stateE};

    String[] stateNames = new String[]{"CPU Usage: (0%, 25%)", "CPU Usage: (25%, 50%)", "CPU Usage: (50%, 75%)", "CPU Usage: (75%, 100%)", "CPU Usage: UnGet"};
    String[] actionNames = new String[]{"Block", "Accept"};

    private volatile double utilityIncrease;
    private volatile int state;
    private volatile int action;

    private volatile int statesCount = 5;
    private volatile int actionsCount = 2;
    private volatile double[][] Q = new double[statesCount][actionsCount];

    private volatile int maxTrainNum = 100000;
    public volatile boolean isTrain = true;

    private volatile int trainNum = 0;


//    /**
//     * 系统的listener
//     */
//    private static AtomicBoolean checkSystemStatus = new AtomicBoolean(false);
//
//    private static SystemStatusListener statusListener = null;
//    private final static SystemRuleManager.SystemPropertyListener listener = new SystemRuleManager.SystemPropertyListener();
//    private static SentinelProperty<List<SystemRule>> currentProperty = new DynamicSentinelProperty<List<SystemRule>>();
//
//    @SuppressWarnings("PMD.ThreadPoolCreationRule")
//    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
//            new NamedThreadFactory("sentinel-system-status-record-task", true));
//
//    static {
//        checkSystemStatus.set(false);
//        statusListener = new SystemStatusListener();
//        scheduler.scheduleAtFixedRate(statusListener, 5, 1, TimeUnit.SECONDS);
//        currentProperty.addListener(listener);
//    }


    private QLearningMetric() {

    }

    public synchronized void setUtilityIncrease(double utilityIncrease) {
        this.utilityIncrease = utilityIncrease;
    }

    public double getUtilityIncrease() {
        return utilityIncrease;
    }

//    public synchronized int locateState() {
//        double currentCpuUsage = statusListener.getCpuUsage();
//        if(0 <= currentCpuUsage && currentCpuUsage < 0.25) {
//            state = 1;return state; }
//        if(0.25 <= currentCpuUsage && currentCpuUsage < 0.5){
//            state = 2;return state;}
//        if(0.5 <= currentCpuUsage && currentCpuUsage < 0.75){
//            state = 3;return state;}
//        if(0.75 <= currentCpuUsage && currentCpuUsage <= 1){
//            state = 4;return state;}
////        if(0 <= currentCpuUsage && currentCpuUsage < 0.25){
////            state = 4;}
////        if(currentCpuUsage<0){
////            //无法获取cpuusage，容错
//
//        state = 0;
//        return state;
//    }


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

    public int getMaxTrainNum() {
        return maxTrainNum;
    }

    public boolean isTrain() {
        return isTrain;
    }

    public synchronized void setTrain(boolean train) {
        isTrain = train;
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

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }
}
