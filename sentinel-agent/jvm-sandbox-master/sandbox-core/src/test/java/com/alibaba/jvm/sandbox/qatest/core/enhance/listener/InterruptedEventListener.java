package com.alibaba.jvm.sandbox.qatest.core.enhance.listener;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.core.enhance.annotation.Interrupted;

@Interrupted
public class InterruptedEventListener implements EventListener {
    @Override
    public void onEvent(Event event) throws Throwable {

    }
}
