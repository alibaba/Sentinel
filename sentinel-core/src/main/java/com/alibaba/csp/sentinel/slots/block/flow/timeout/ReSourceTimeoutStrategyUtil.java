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
package com.alibaba.csp.sentinel.slots.block.flow.timeout;

import io.netty.util.Timeout;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yunfeiyanggzq
 */
public class ReSourceTimeoutStrategyUtil {
    private final static ConcurrentHashMap<Long, Timeout> TimeoutMap = new ConcurrentHashMap<>();

    private ReSourceTimeoutStrategyUtil() {

    }

    public static ReSourceTimeoutStrategy getTimeoutStrategy(int resourceTimeoutStrategyStatus) {
        return new ReleaseTokenStrategy();
    }

    public static void addTimeout(Long tokenId, Timeout timeout) {
        if (timeout != null) {
            TimeoutMap.put(tokenId, timeout);
        }
    }

    public static Timeout getTimeout(Long tokenId) {
        return TimeoutMap.get(tokenId);
    }

    public static void removeTimeout(Long tokenId) {
        TimeoutMap.remove(tokenId);
    }

    public static boolean containsTokenId(Long tokenId) {
        return TimeoutMap.containsKey(tokenId);
    }

    public static void clearByTokenId(Long tokenId) {
        if (tokenId == null) {
            return;
        }
        removeTimeout(tokenId);
    }

    public static int getTimeoutSize() {
        return TimeoutMap.size();
    }
}
