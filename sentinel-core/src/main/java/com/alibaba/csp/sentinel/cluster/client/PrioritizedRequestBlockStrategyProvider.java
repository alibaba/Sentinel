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
package com.alibaba.csp.sentinel.cluster.client;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.PrioritizedRequestBlockStrategy;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * @author yunfeiyanggzq
 */
public class PrioritizedRequestBlockStrategyProvider {
    private static PrioritizedRequestBlockStrategy strategy = null;

    static {
        // Not strictly thread-safe, but it's OK since it will be resolved only once.
        resolveStrategyInstance();
    }

    public static PrioritizedRequestBlockStrategy getStrategy() {
        return strategy;
    }

    private static void resolveStrategyInstance() {
        PrioritizedRequestBlockStrategy blockStrategy = SpiLoader.loadFirstInstance(PrioritizedRequestBlockStrategy.class);
        if (blockStrategy == null) {
            RecordLog.info(
                    "[PrioritizedRequestBlockStrategyProvider] No existing strategy, release token strategy will not be activated");
        } else {
            strategy = blockStrategy;
            RecordLog.info(
                    "[PrioritizedRequestBlockStrategyProvider] default block strategy resolved: " + strategy.getClass().getCanonicalName());
        }
    }

    public static boolean isStrategySpiAvailable() {
        return getStrategy() != null;
    }

    private PrioritizedRequestBlockStrategyProvider() {
    }

}
