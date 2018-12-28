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
package com.alibaba.csp.sentinel.transport;

/**
 * The heartbeat sender which is responsible for sending heartbeat to remote dashboard
 * periodically per {@code interval}.
 *
 * @author leyou
 * @author Eric Zhao
 */
public interface HeartbeatSender {

    /**
     * Send heartbeat to Sentinel Dashboard. Each invocation of this method will send
     * heartbeat once. Sentinel core is responsible for invoking this method
     * at every {@link #intervalMs()} interval.
     *
     * @return whether heartbeat is successfully send.
     * @throws Exception if error occurs
     */
    boolean sendHeartbeat() throws Exception;

    /**
     * Default interval in milliseconds of the sender. It would take effect only when
     * the heartbeat interval is not configured in Sentinel config property.
     *
     * @return default interval of the sender in milliseconds
     */
    long intervalMs();
}
