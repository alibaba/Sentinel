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

import com.alibaba.csp.sentinel.event.publisher.DefaultEventPublisher;
import com.alibaba.csp.sentinel.event.subscriber.DefaultEventSubscriber;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class DefaultEventPublisherTest {

    @Test
    public void testDefaultEventPublisher() {

        DefaultEventPublisher publisher = new DefaultEventPublisher();
        Assert.assertTrue(publisher.getEventSubscribers().size() == 0);

        DefaultEventSubscriber subscriber1 = new DefaultEventSubscriber();
        publisher.addSubscriber(subscriber1);
        Assert.assertTrue(publisher.getEventSubscribers().size() == 1);
        Assert.assertTrue(publisher.getEventSubscribers().contains(subscriber1));

        DefaultEventSubscriber subscriber2 = new DefaultEventSubscriber();
        publisher.addSubscriber(subscriber2);
        Assert.assertTrue(publisher.getEventSubscribers().size() == 2);
        Assert.assertTrue(publisher.getEventSubscribers().contains(subscriber1));
        Assert.assertTrue(publisher.getEventSubscribers().contains(subscriber2));

        publisher.removeSubscriber(subscriber1);
        Assert.assertFalse(publisher.getEventSubscribers().contains(subscriber1));
        Assert.assertTrue(publisher.getEventSubscribers().contains(subscriber2));

        publisher.removeSubscriber(subscriber2);
        Assert.assertFalse(publisher.getEventSubscribers().contains(subscriber1));
        Assert.assertFalse(publisher.getEventSubscribers().contains(subscriber2));


    }
}
