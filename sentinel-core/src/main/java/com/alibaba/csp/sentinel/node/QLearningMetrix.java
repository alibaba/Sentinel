package com.alibaba.csp.sentinel.node;

/**
 * @Author: Go Bear
 * @Description:Read and load information form json file
 * @Date:Created in 11:44 AM 2020/6/22
 */
public class QLearningMetrix {
    private double test;

    private QLearningMetrix(){

    }

    public synchronized void setTest(double test) {
        this.test = test;
    }

    public double getTest() {
        return test;
    }

    private static class QLearningMetrixContainer{
        private static QLearningMetrix instance = new QLearningMetrix();
    }
    public static QLearningMetrix getInstance(){
        return QLearningMetrixContainer.instance;
    }
}
