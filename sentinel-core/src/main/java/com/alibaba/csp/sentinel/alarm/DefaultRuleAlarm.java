package com.alibaba.csp.sentinel.alarm;

import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p> Description:
 * <p>
 *
 * @author kangyl
 * @since 1.0
 */
public class DefaultRuleAlarm<T> implements RuleAlarm<T> {

    private Set<RuleAlarmListener<T>> listeners = Collections.synchronizedSet(new HashSet<RuleAlarmListener<T>>());

    @Override
    public void addListener(RuleAlarmListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RuleAlarmListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void triggerAlarm(T rule) {
        RecordLog.info("[DefaultRuleAlarm]  will trigger a warning: " + rule);

        for (RuleAlarmListener<T> listener : listeners) {
            listener.warn(rule);
        }
    }
}
