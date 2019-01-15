package com.alibaba.csp.sentinel.alarm;

/**
 * <p> Description:
 * <p>
 *
 * @author kangyl
 * @since 1.0
 */
public interface RuleAlarm<T> {

    /**
     * <p>
     * Add a {@link RuleAlarmListener} to this {@link RuleAlarm}. After the listener is added,
     * {@link #triggerAlarm(Object)} will inform the listener if needed.
     * </p>
     * <p>
     * This method can invoke multi times to add more than one listeners.
     * </p>
     *
     * @param listener listener to add.
     */
    void addListener(RuleAlarmListener<T> listener);

    /**
     * Remove the {@link RuleAlarmListener} on this. After removing, {@link #triggerAlarm(Object)}
     * will not inform the listener.
     *
     * @param listener the listener to remove.
     */
    void removeListener(RuleAlarmListener<T> listener);

    /**
     * Publish the {@code value} to all {@link RuleAlarmListener}s
     *
     * @param value the rule.
     */
    void triggerAlarm(T value);
}
