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
package com.alibaba.csp.sentinel.heartbeat;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiLoader;
import com.alibaba.csp.sentinel.transport.message.HeartbeatMessage;

/**
 * @author wxq
 * @since 1.8.2
 */
public final class HeartbeatMessageProvider {

    private static final HeartbeatMessage heartbeatMessage = resolveInstance();

    private HeartbeatMessageProvider() {
    }

    private static HeartbeatMessage resolveInstance() {
        HeartbeatMessage heartbeatMessage = SpiLoader.of(HeartbeatMessage.class).loadHighestPriorityInstance();
        if (heartbeatMessage == null) {
            RecordLog.warn("[HeartbeatMessageProvider] WARN: No existing HeartbeatMessage found");
        } else {
            RecordLog.info("[HeartbeatMessageProvider] HeartbeatMessage activated: {}", heartbeatMessage.getClass()
                    .getCanonicalName());
        }
        return heartbeatMessage;
    }

    /**
     * @return resolved {@link HeartbeatMessage}.
     */
    public static HeartbeatMessage getHeartbeatMessage() {
        return heartbeatMessage;
    }

}
