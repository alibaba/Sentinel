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
package com.alibaba.csp.sentinel.event;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.event.degrade.AbstractDegradeEventSubscriber;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lianglin
 * @since  1.7.0
 */
public abstract class AbstractEventPublisher<T> implements EventFilter<T>, EventPublisher<T>{

     private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2 * Runtime.getRuntime().availableProcessors(),
            60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
            new NamedThreadFactory("sentinel-event-publish-task", true));

    protected List<EventSubscriber> eventSubscribers = new ArrayList<>();


    @Override
    public void publish(final Event<T> event) {
        if (filter(event)) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (EventSubscriber subscriber : eventSubscribers) {
                        subscriber.listen(event);
                    }
                }
            });
        }
    }

}
