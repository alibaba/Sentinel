package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

/**
 * Define the functions of the scene threshold configuration class.
 *
 * @author ylnxwlp
 */
public interface ScenarioConfig {

    int getRecoveryTimeoutMs();

    long getHalfOpenTimeoutMs();
}
