package com.alibaba.csp.sentinel.alarm;

/**
 * <p> Description:
 * This class holds callback method when {@link RuleAlarm#triggerAlarm(Object)}  need inform the listener
 * <p>
 *
 * @author kangyl
 * @since 1.0
 */
public interface RuleAlarmListener<T> {

    /**
     * Callback method when {@link RuleAlarm#triggerAlarm(Object)}  need inform the listener.
     *
     * @param rule actual rule.
     */
    void warn(T rule);
}
