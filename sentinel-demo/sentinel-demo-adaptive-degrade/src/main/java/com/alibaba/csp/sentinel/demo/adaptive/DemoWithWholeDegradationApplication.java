package com.alibaba.csp.sentinel.demo.adaptive;

/**
 * Fully degraded the operation class.
 *
 * @author ylnxwlp
 */
public class DemoWithWholeDegradationApplication {
    public static void main(String[] args) throws InterruptedException {
        DemoPreparation.runSimulationWithWholeDegradation();
    }
}