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
package com.alibaba.csp.sentinel.transport.message;

import org.junit.Test;

import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageKeyConstants.CURRENT_TIME_MILLIS;
import static org.junit.Assert.*;

import java.util.UUID;

/**
 * @author wxq
 * @since 1.8.2
 */
public class DefaultHeartbeatMessageTest {

    /**
     * key value pair's count.
     *
     * @see HeartbeatMessageKeyConstants for how many keys
     */
    @Test
    public void testKeyValuesSize() {
        HeartbeatMessage heartbeatMessage = new DefaultHeartbeatMessage();
        int size = heartbeatMessage.get().size();

        // should change here when add a new key value pair
        assertEquals(8, size);
    }

    @Test
    public void testInformationCurrentTimeMillis() throws InterruptedException {
        HeartbeatMessage heartbeatMessage = new DefaultHeartbeatMessage();
        assertTrue(heartbeatMessage.get().containsKey(CURRENT_TIME_MILLIS));
        String time1 = heartbeatMessage.get().get(CURRENT_TIME_MILLIS);
        Thread.sleep(2);
        String time2 = heartbeatMessage.get().get(CURRENT_TIME_MILLIS);
        assertTrue(Long.parseLong(time2) > Long.parseLong(time1));
    }

    @Test
    public void testSetInformation() {
    	HeartbeatMessage heartbeatMessage = new DefaultHeartbeatMessage();
    	String key = UUID.randomUUID().toString();
    	String value = UUID.randomUUID().toString();
    	assertFalse(heartbeatMessage.get().containsKey(key));
    	
    	heartbeatMessage.setInformation(key, value);
    	assertEquals(value, heartbeatMessage.get().get(key));
    }

}