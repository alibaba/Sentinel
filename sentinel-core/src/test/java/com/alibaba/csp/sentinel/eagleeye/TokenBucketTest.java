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
package com.alibaba.csp.sentinel.eagleeye;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TokenBucketTest {

    @Test(expected = IllegalArgumentException.class)
    public void testTokenBucketFailToken() {
        TokenBucket tokenBucket = new TokenBucket(-1, TimeUnit.SECONDS.toMillis(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTokenBucketFailTime() {
        TokenBucket tokenBucket = new TokenBucket(10, 10);
    }

    @Test
    public void testAccept() {
        TokenBucket tokenBucket = new TokenBucket(1, TimeUnit.SECONDS.toMillis(10));
        Assert.assertTrue(tokenBucket.accept(System.currentTimeMillis()));
        Assert.assertFalse(tokenBucket.accept(System.currentTimeMillis()));
    }
}
