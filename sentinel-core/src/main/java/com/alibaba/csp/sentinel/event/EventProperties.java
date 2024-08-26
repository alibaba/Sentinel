/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

/**
 * Default impl for SentinelEventMulticaster.
 *
 * @author Daydreamer-ia
 */
public class EventProperties {

    /**
     * max queue size for event.
     */
    public static final String MAX_EVENT_QUEUE_SIZE = "sentinel.event.queue.max-size";

}
