package com.alibaba.csp.sentinel.qlearning;

/**
 * @Author: Go Bear
 * @Description:Read and load information form json file
 * @Date:Created in 11:44 AM 2020/6/22
 */
public class QLearningMetric {
    private volatile double utilityIncrease;
    private volatile int state;

    private volatile int action;

    private volatile int statesCount = 5;
    private volatile int actionsCount = 2;
    public volatile double[][] Q = new double[statesCount][actionsCount];

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

    public double getQValue(int state, int action){
        return this.Q[state][action];
    }


    public synchronized void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public double getmaxQ(int nextState) {
        return 0;
    }


    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }
}
