/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.publisher;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.event.Event;
import com.alibaba.csp.sentinel.event.subscriber.EventSubscriber;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class DefaultEventPublisher implements EventPublisher {


    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService pool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), 2 * Runtime.getRuntime().availableProcessors(), 60L, TimeUnit.SECONDS
            , new ArrayBlockingQueue<Runnable>(1000)
            , new NamedThreadFactory("sentinel-event-publish-task", true)
            , new ThreadPoolExecutor.AbortPolicy());

    private final Set<EventSubscriber> eventSubscribers = new HashSet<>();

    @Override
    public void publish(Event event) {
        try {
            for (EventSubscriber subscriber : eventSubscribers) {
                subscriber.listen(event);
            }
        } catch (Exception ex) {
            RecordLog.warn("[DefaultEventPublisher] publish event error ", ex);
        }

    }

    @Override
    public void asyPublish(Event event) {
        pool.submit(new PublishEventTask(event));
    }

    @Override
    public void addSubscriber(EventSubscriber subscriber) {
        eventSubscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(EventSubscriber subscriber) {
        eventSubscribers.remove(subscriber);
    }

    @Override
    public void close() {
        pool.shutdown();
        eventSubscribers.clear();
    }

    /**
     * For test
     *
     * @return
     */
    public Set<EventSubscriber> getEventSubscribers() {
        return eventSubscribers;
    }

    private class PublishEventTask implements Runnable {

        private Event event;

        public PublishEventTask(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                for (EventSubscriber subscriber : eventSubscribers) {
                    subscriber.listen(event);
                }
            } catch (Exception ex) {
                RecordLog.warn("[DefaultEventPublisher] publish event error ", ex);
            }
        }
    }

}
