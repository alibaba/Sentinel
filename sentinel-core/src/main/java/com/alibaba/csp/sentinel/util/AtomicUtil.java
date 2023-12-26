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
package com.alibaba.csp.sentinel.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class AtomicUtil {
    /**
     * use updateAndGet atomic effort-side to update rules
     *
     * @param consumer consumer of append or delete function
     * @return is update success
     */
    public static <T> boolean atomicUpdate(AtomicLong postLock, Supplier<List<T>> consumer) {
        final boolean[] result = {false};

        while (!result[0]) {
            //get a long num as a lock to promise consumer is atomic
            long oldLock = postLock.get();
            postLock.updateAndGet(operand -> {
                if (operand == oldLock) {
                    consumer.get();
                    result[0] = true;
                }
                return System.currentTimeMillis();
            });
        }

        return result[0];
    }
}
