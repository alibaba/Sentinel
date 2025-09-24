package com.alibaba.csp.sentinel.demo.adaptive;

/**
 * Non-fully degraded operation class.
 *
 * @author ylnxwlp
 */
public class DemoWithoutWholeDegradationApplication {
    public static void main(String[] args) throws InterruptedException {
        DemoPreparation.runSimulationWithoutWholeDegradation();
    }
}