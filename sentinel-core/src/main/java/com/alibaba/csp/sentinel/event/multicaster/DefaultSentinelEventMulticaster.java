/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.event.multicaster;

import com.alibaba.csp.sentinel.event.EventProperties;
import com.alibaba.csp.sentinel.event.SentinelEventListener;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default impl for SentinelEventMulticaster.
 *
 * @author Daydreamer-ia
 */
@SuppressWarnings("all")
public class DefaultSentinelEventMulticaster implements SentinelEventMulticaster, Runnable {


    private Properties properties;

    /**
     * registry.
     */
    private SentinelEventListenerRegistry registry;

    /**
     * event queue.
     */
    private BlockingQueue<SentinelEvent> queue;

    /**
     * init mark.
     */
    private volatile boolean running = false;

    /**
     * core thread.
     */
    private final Thread thread = new Thread(this);

    /**
     * used to record event sequence.
     */
    private Map<Class<? extends SentinelEvent>, AtomicLong> sequences = new ConcurrentHashMap<>();

    @Override
    public void init(Properties properties, SentinelEventListenerRegistry registry) {
        this.properties = properties;
        this.registry = registry;
        doInit();
    }

    private void doInit() {
        String queueSize = properties.getProperty(EventProperties.MAX_EVENT_QUEUE_SIZE, "16384");
        this.queue = new ArrayBlockingQueue<>(Integer.parseInt(queueSize));
        running = true;
        thread.start();
    }

    @Override
    public void destroy() {
        this.running = false;
        this.queue.clear();
    }

    @Override
    public boolean publish(SentinelEvent event) {
        boolean success = queue.offer(event);
        // if no success, handle by submitted thread
        if (!success) {
            receiveEvent(event);
        }
        return true;
    }

    @Override
    public void run() {
        try {
            while (running) {
                SentinelEvent poll = queue.take();
                if (poll != null) {
                    // handle event
                    receiveEvent(poll);
                    // increase sequence
                    increaseSequence(poll.getClass());
                }
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * notify event to all listeners.
     *
     * @param event event
     */
    protected void receiveEvent(SentinelEvent event) {
        Class<? extends SentinelEvent> eventType = event.getClass();
        List<SentinelEventListener<? extends SentinelEvent>> listeners = registry.getSentinelEventListener(eventType);
        if (listeners != null && !listeners.isEmpty()) {
            // notify event
            for (SentinelEventListener<? extends SentinelEvent> listener : listeners) {
                notifyListeners(event, listener);
            }
        }
    }

    /**
     * notify specify listener.
     *
     * @param event    event
     * @param listener listener
     */
    protected void notifyListeners(SentinelEvent event, SentinelEventListener listener) {
        // get event sequence
        long sequence = event.getSequence();
        AtomicLong lastSequence = sequences.compute(event.getClass(), (k, v) -> v == null ? new AtomicLong(0) : v);
        // does it abandon this event
        if (listener.alwaysLast() && sequence < lastSequence.get()) {
            RecordLog.debug("[SingleThreadEventMulticaster] Abandon event because listener appreciate last event, current sequence: {}, event sequence: {}",
                    lastSequence, sequence);
            return;
        }
        // does it appreciate to handle event asyc
        if (listener.executor() != null) {
            listener.executor().execute(() -> listener.onEvent(event));
        }
        // handle by callback thread
        listener.onEvent(event);
    }

    /**
     * increase event sequnce to support event expired.
     *
     * @param eventType event
     */
    protected void increaseSequence(Class<? extends SentinelEvent> eventType) {
        sequences.compute(eventType, (k, v) -> v == null ? new AtomicLong(0) : v).incrementAndGet();
    }

}
