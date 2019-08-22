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
package com.alibaba.csp.sentinel.event.subscriber;

import com.alibaba.csp.sentinel.event.subscriber.DefaultEventSubscriber;
import com.alibaba.csp.sentinel.event.subscriber.EventSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author lianglin
 * @since 1.7.0
 */
public final class EventSubscriberProvider {

    private static final ServiceLoader<EventSubscriber> subscribersLoader = ServiceLoader.load(EventSubscriber.class);

    public static List<EventSubscriber> provide() {
        List<EventSubscriber>  subscribers = new ArrayList<>();
        for (EventSubscriber subscriber : subscribersLoader) {
            if (subscriber != null && subscriber.getClass() != DefaultEventSubscriber.class) {
                 subscribers.add(subscriber);
            }
        }
        subscribers.add(new DefaultEventSubscriber());
        return subscribers;
    }
}
