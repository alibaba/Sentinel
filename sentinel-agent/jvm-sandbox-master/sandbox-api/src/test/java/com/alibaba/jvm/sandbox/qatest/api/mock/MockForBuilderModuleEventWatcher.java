package com.alibaba.jvm.sandbox.qatest.api.mock;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchCondition;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;

import java.util.concurrent.atomic.AtomicReference;

public class MockForBuilderModuleEventWatcher implements ModuleEventWatcher {

    private final AtomicReference<EventWatchCondition> eventWatchConditionRef
            = new AtomicReference<EventWatchCondition>();

    private final AtomicReference<EventListener> eventListenerRef
            = new AtomicReference<EventListener>();

    private final AtomicReference<Progress> progressRef
            = new AtomicReference<Progress>();

    private final AtomicReference<Event.Type[]> eventTypeArrayRef
            = new AtomicReference<Event.Type[]>();

    public EventWatchCondition getEventWatchCondition() {
        return eventWatchConditionRef.get();
    }

    public EventListener getEventListener() {
        return eventListenerRef.get();
    }

    public Progress getProgress() {
        return progressRef.get();
    }

    public Event.Type[] getEventTypeArray() {
        return eventTypeArrayRef.get();
    }

    @Override
    public int watch(Filter filter, EventListener listener, Progress progress, Event.Type... eventType) {
        return 0;
    }

    @Override
    public int watch(Filter filter, EventListener listener, Event.Type... eventType) {
        return 0;
    }

    @Override
    public int watch(EventWatchCondition condition, EventListener listener, Progress progress, Event.Type... eventType) {
        eventWatchConditionRef.set(condition);
        eventListenerRef.set(listener);
        progressRef.set(progress);
        eventTypeArrayRef.set(eventType);
        return 0;
    }

    @Override
    public void delete(int watcherId, Progress progress) {

    }

    @Override
    public void delete(int watcherId) {

    }

    @Override
    public void watching(Filter filter, EventListener listener, Progress wProgress, WatchCallback watchCb, Progress dProgress, Event.Type... eventType) throws Throwable {

    }

    @Override
    public void watching(Filter filter, EventListener listener, WatchCallback watchCb, Event.Type... eventType) throws Throwable {

    }
}
