/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.trust.auth.condition.matcher;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lwj
 * @since 2.0.0
 */
public class IpMatcherTest {

    @Test
    public void testIp2BinaryString() {
        assertEquals("01111111000000000000000000000001", IpMatcher.ip2BinaryString("127.0.0.1"));
        assertEquals("", IpMatcher.ip2BinaryString("127.0.0"));
        assertEquals("11111111111111111111111111111111", IpMatcher.ip2BinaryString("255.255.255.255"));
        assertEquals("", IpMatcher.ip2BinaryString("256.0.0.1"));
        assertEquals("", IpMatcher.ip2BinaryString("127.0.0.0.1"));
        assertEquals("", IpMatcher.ip2BinaryString("-127.0.0.0.1"));
        assertEquals("", IpMatcher.ip2BinaryString("-xx.0.0.0.1"));
    }

    @Test
    public void testMatch() {
        IpMatcher ipMatcher = new IpMatcher(32, "127.0.0.1");
        Assert.assertTrue(ipMatcher.match("127.0.0.1"));
        Assert.assertFalse(ipMatcher.match("127.0.0.2"));

        ipMatcher = new IpMatcher(8, "127.0.0.0");
        Assert.assertTrue(ipMatcher.match("127.0.0.0"));
        Assert.assertTrue(ipMatcher.match("127.255.0.0"));
        Assert.assertFalse(ipMatcher.match("128.0.0.0"));
    }
}