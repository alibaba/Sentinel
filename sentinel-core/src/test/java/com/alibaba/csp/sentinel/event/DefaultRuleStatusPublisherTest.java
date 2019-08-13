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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class DefaultRuleStatusPublisherTest {

    @Test
    public void testDefaultRulePublisher() {

        DefaultRuleStatusPublisher publisher = new DefaultRuleStatusPublisher();
        Assert.assertTrue(publisher.getEventSubscribers().size() == 0);

        DefaultRuleStatusSubscriber subscriber1 = new DefaultRuleStatusSubscriber();
        publisher.addSubscriber(subscriber1);
        Assert.assertTrue(publisher.getEventSubscribers().size() == 1);
        Assert.assertTrue(publisher.getEventSubscribers().contains(subscriber1));

        DefaultRuleStatusSubscriber subscriber2 = new DefaultRuleStatusSubscriber();
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
