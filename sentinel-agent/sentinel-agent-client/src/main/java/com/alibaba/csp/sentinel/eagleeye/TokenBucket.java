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

import java.util.concurrent.atomic.AtomicLong;

class TokenBucket {

    private final long maxTokens;

    private final long intervalMillis;

    private volatile long nextUpdate;

    private AtomicLong tokens;

    public TokenBucket(long maxTokens, long intervalMillis) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens should > 0, but given: " + maxTokens);
        }
        if (intervalMillis < 1000) {
            throw new IllegalArgumentException("intervalMillis should be at least 1000, but given: " + intervalMillis);
        }
        this.maxTokens = maxTokens;
        this.intervalMillis = intervalMillis;
        this.nextUpdate = System.currentTimeMillis() / 1000 * 1000 + intervalMillis;
        this.tokens = new AtomicLong(maxTokens);
    }

    public boolean accept(long now) {
        long currTokens;
        if (now > nextUpdate) {
            currTokens = tokens.get();
            if (tokens.compareAndSet(currTokens, maxTokens)) {
                nextUpdate = System.currentTimeMillis() / 1000 * 1000 + intervalMillis;
            }
        }

        do {
            currTokens = tokens.get();
        } while (currTokens > 0 && !tokens.compareAndSet(currTokens, currTokens - 1));

        return currTokens > 0;
    }
}
